package honstain

import com.codahale.metrics.annotation.Timed
import com.fasterxml.jackson.databind.ObjectMapper
import honstain.api.Inventory
import honstain.api.InventoryWithProduct
import honstain.api.Product
import honstain.client.ProductJerseyRXClient
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.concurrent.CompletableFuture
import javax.ws.rs.*
import javax.ws.rs.core.MediaType


@Path("/inventory")
@Produces(MediaType.APPLICATION_JSON)
class InventoryResource(
        val productClient: ProductJerseyRXClient,
        val productCache: MutableMap<Long, Product>,
        val consumer: Consumer<String?, String?>,
        val objectMapper: ObjectMapper,
) {

    val log: Logger = LoggerFactory.getLogger(InventoryResource::class.java)

    val locationToProduct = mutableMapOf(
            5L to mutableSetOf(
                    1L,
                    2L,
                    3L,
            ),
            6L to mutableSetOf(
                    4L,
                    5L,
            ),
    )

    val inventoryRecords = mutableMapOf(
            Pair(5L,1L) to Inventory(5,1, 5),
            Pair(5L,2L) to Inventory(5,2, 5),
            Pair(5L,3L) to Inventory(5,3, 5),
            Pair(6L,4L) to Inventory(6,4, 1),
            Pair(6L,5L) to Inventory(6,5, 1),
    )

    @GET
    fun getAll(): List<Inventory> {
        return inventoryRecords.values.toList()
    }

    @GET
    @Timed
    @Path("/{locationId}")
    fun getSingle(@PathParam("locationId") locationId: Long): List<Inventory> {
        val products: MutableSet<Long> = locationToProduct.getOrElse(locationId, {
            throw NotFoundException("There is no inventory for locationId:$locationId")
        })

        val result = mutableListOf<Inventory>()
        for(product in products){
            result.add(
                    inventoryRecords.getOrElse(
                            Pair(locationId, product),
                            { throw WebApplicationException() }
                    )
            )
        }
        return result
    }

    @GET
    @Timed
    @Path("/{locationId}/withProduct")
    fun getSingleWithProduct(
            @PathParam("locationId") locationId: Long,
    ): List<InventoryWithProduct> {
        log.debug("getSingleWithProduct $locationId")

        val products: MutableSet<Long> = locationToProduct.getOrElse(locationId, {
            throw NotFoundException("There is no inventory for locationId:$locationId")
        })

        val step1: List<CompletableFuture<Inventory>> = products.map { productId ->
            getInventoryFuture(locationId, productId)
        }

        val step2: List<CompletableFuture<Product>> = step1.map { foo ->
            foo.thenCompose { bar ->
                if (productCache.containsKey(bar.productId)) {
                    log.debug("cache HIT locationId:${bar.locationId} productId:${bar.productId}")
                    val future = CompletableFuture<Product>()
                    future.complete(productCache.get(bar.productId))
                    future
                } else {
                    log.debug("cache MISS locationId:${bar.locationId} productId:${bar.productId}")
                    productClient.getProduct(bar.productId).toCompletableFuture()
                }
            }
        }

        val step3: List<CompletableFuture<Product>> = step2.map {foo ->
            foo.thenApply { bar ->
                log.debug("Product service client call for product: ${bar.productId} retrieved ${bar.sku}")
                bar
            }
        }

        val step4: List<CompletableFuture<InventoryWithProduct>> = step1.zip(step3)  { foo, bar ->
            foo.thenCombine(bar) {
                foo1, bar1 -> InventoryWithProduct(foo1.locationId, foo1.productId, foo1.quantity, bar1.sku)
            }
        }

        val result: List<InventoryWithProduct> = step4.map { foo -> foo.join() }
                .map { bar ->
                    log.debug("InventoryWithProduct ${bar.locationId}, ${bar.productId}, ${bar.sku}")
                    bar
                }
       return result
    }

    /*
    getInventoryPlain - This helper function was used in conjunction with a supplyAsync call like:
                    CompletableFuture.supplyAsync { getInventoryPlain(locationId, productId) }
    Note the goal of these helper functions was to assist in creating a chain of async calls in a reactive style.

    They are meant to simulate a DB call (which I have done with a thread sleep at time), but I didn't hook a DB
    in order to simplify my experiments.
     */
    fun getInventoryPlain(locationId: Long, productId: Long): Inventory {
        val inventory: Inventory = inventoryRecords.getOrElse(Pair(locationId, productId),{ throw WebApplicationException() })
        //Thread.sleep(1000 * max(1, 6 - productId)) // Doing this so the first calls wait longer

        log.debug("Local call for inventory:${inventory.locationId} RESULT - will need to get product:$productId")

        return inventory
    }

    /*
    getInventoryFuture - This helper function returns a basic CompletableFuture so that it could be chained.

    Note the goal of these helper functions was to assist in creating a chain of async calls in a reactive style.

    They are meant to simulate a DB call (which I have done with a thread sleep at time), but I didn't hook a DB
    in order to simplify my experiments.
     */
    fun getInventoryFuture(locationId: Long, productId: Long): CompletableFuture<Inventory> {
        val inventory: Inventory = inventoryRecords.getOrElse(Pair(locationId, productId),{ throw WebApplicationException() })
        log.debug("For inventory:${inventory.locationId} get product:$productId")

        val future = CompletableFuture<Inventory>()
        future.complete(inventory)
        return future
    }


    @POST
    @Timed
    fun create(inventory: Inventory): Inventory {

        locationToProduct.getOrPut(inventory.locationId, { mutableSetOf() })
                .add(inventory.productId)

        this.inventoryRecords[Pair(inventory.locationId, inventory.productId)] = inventory
        return inventory
    }

    @PUT
    @Timed
    @Path("/{locationId}")
    fun update(@PathParam("locationId") locationId: Long, inventory: Inventory): Inventory {
        if (locationId != inventory.locationId) throw BadRequestException()

        this.inventoryRecords.getOrElse(Pair(inventory.locationId, inventory.productId), {
            throw NotFoundException("There is no inventory for locationId:$locationId and productId:${inventory.productId}")
        })

        this.inventoryRecords[Pair(inventory.locationId, inventory.productId)] = inventory
        return inventory
    }

    @POST
    @Timed
    @Path("/consume/productDLT")
    fun consumeProductDLT(): Int {
        var totalRecordsProcessed = 0
        try {
            //consumer.subscribe(listOf("dlq-product"))

            var previousBatch = 1
            var attempts = 3

            while (attempts-- > 0 || previousBatch > 0) {
                val records: ConsumerRecords<String?, String?> = consumer.poll(Duration.ofMillis(100))
                log.info("Batchsize: ${records.count()}")
                for (record in records) {
                    log.info("${record.topic()} ${record.offset()}, ${record.value()}")
                    val foo: String = record.value()!!
                    val bar = objectMapper.readValue(foo, Product::class.java)
                    productCache[bar.productId] = bar
                    //Thread.sleep(100)
                }
                totalRecordsProcessed += records.count()
                previousBatch = records.count()
            }
        }
        catch (e: Exception) {
            log.error("Failed to consume message: ${e.message}")
        }
        //finally {
        //    consumer.wakeup()
        //}
        return totalRecordsProcessed
    }
}