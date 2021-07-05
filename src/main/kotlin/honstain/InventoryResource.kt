package honstain

import com.codahale.metrics.annotation.Timed
import honstain.api.Inventory
import honstain.api.InventoryWithProduct
import honstain.client.ProductClient
import javax.ws.rs.*
import javax.ws.rs.core.MediaType


@Path("/inventory")
@Produces(MediaType.APPLICATION_JSON)
class InventoryResource(val productClient: ProductClient) {

    val locationToProduct = mutableMapOf(
            5L to mutableSetOf(1L, 2L, 3L)
    )

    val inventoryRecords = mutableMapOf(
            Pair(5L,1L) to Inventory(5,1, 5),
            Pair(5L,2L) to Inventory(5,2, 5),
            Pair(5L,3L) to Inventory(5,3, 5),
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
    fun getSingleWithProduct(@PathParam("locationId") locationId: Long): List<InventoryWithProduct> {
        val products: MutableSet<Long> = locationToProduct.getOrElse(locationId, {
            throw NotFoundException("There is no inventory for locationId:$locationId")
        })

        val result = mutableListOf<InventoryWithProduct>()
        for(productId in products){
            val inventory: Inventory = inventoryRecords.getOrElse(Pair(locationId, productId),{ throw WebApplicationException() })
            val product = productClient.getProduct(productId)
            result.add(InventoryWithProduct(
                    inventory.locationId,
                    inventory.productId,
                    inventory.quantity,
                    product.sku,
            ))
        }
        return result
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
}