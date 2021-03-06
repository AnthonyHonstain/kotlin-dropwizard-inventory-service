import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import honstain.InventoryResource
import honstain.api.Inventory
import honstain.api.InventoryWithProduct
import honstain.api.Product
import honstain.client.ProductJerseyRXClient
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport
import io.dropwizard.testing.junit5.ResourceExtension
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.apache.kafka.clients.consumer.Consumer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import javax.ws.rs.client.Entity
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response


@ExtendWith(DropwizardExtensionsSupport::class)
class InventoryResourceTest {
    /*
    Modeling this test based on the example in the official docs Dropwizard v2.0.23:
    https://www.dropwizard.io/en/latest/manual/testing.html#testing-resources
     */

    val productCache = mutableMapOf<Long, Product>()
    val productClient = mockk<ProductJerseyRXClient>()
    val consumer = mockk<Consumer<String?, String?>>()
    val objectMapper = ObjectMapper().registerModule(KotlinModule())
    val EXT: ResourceExtension = ResourceExtension.builder()
            .addResource(InventoryResource(productClient, productCache, consumer, objectMapper))
            .setMapper(objectMapper)
            .build()

    @BeforeEach
    fun setup() {
        productCache.clear()
    }

    @Test
    fun `GET all inventory`() {
        val result = EXT.target("/inventory/").request().get(object: GenericType<List<Inventory>>() {})
        // TODO - warning: the data is sourced from a dump in-memory hashMap so that I could work around the need for a DB.

        val expected = listOf(
                Inventory(5,1, 5),
                Inventory(5,2, 5),
                Inventory(5,3, 5),
                Inventory(6,4, 1),
                Inventory(6,5, 1),
        )
        assertEquals(expected, result)
    }

    @Test
    fun `GET single inventory`() {
        val result = EXT.target("/inventory/5").request().get(object: GenericType<List<Inventory>>() {})
        // TODO - warning: the data is sourced from a dump in-memory hashMap so that I could work around the need for a DB.

        val expected = listOf(
                Inventory(5,1, 5),
                Inventory(5,2, 5),
                Inventory(5,3, 5),
        )
        assertEquals(expected, result)
    }

    @Test
    fun `GET single inventory with product with product cache`() {
        productCache[1] = Product(1, "SKU-01", null, null)

        val prod2 = mockk<CompletionStage<Product>>()
        val prod3 = mockk<CompletionStage<Product>>()

        every { productClient.getProduct(2) } returns prod2
        every { productClient.getProduct(3) } returns prod3

        every { prod2.toCompletableFuture() } returns CompletableFuture.completedFuture(Product(2, "SKU-02", null, null))
        every { prod3.toCompletableFuture() } returns CompletableFuture.completedFuture(Product(3, "SKU-03", null, null))

        val result = EXT.target("/inventory/5/withProduct").request().get(object: GenericType<List<InventoryWithProduct>>() {})
        // TODO - warning: the data is sourced from a dump in-memory hashMap so that I could work around the need for a DB.

        val expected = listOf(
                InventoryWithProduct(5,1, 5, "SKU-01"),
                InventoryWithProduct(5,2, 5, "SKU-02"),
                InventoryWithProduct(5,3, 5, "SKU-03"),
        )
        assertEquals(expected, result)
        verify(exactly = 0) { productClient.getProduct(1) }
    }

    @Test
    fun `GET single inventory with product and empty cache`() {

        val prod1 = mockk<CompletionStage<Product>>()
        val prod2 = mockk<CompletionStage<Product>>()
        val prod3 = mockk<CompletionStage<Product>>()
        every { productClient.getProduct(1) } returns prod1
        every { productClient.getProduct(2) } returns prod2
        every { productClient.getProduct(3) } returns prod3

        every { prod1.toCompletableFuture() } returns CompletableFuture.completedFuture(Product(1, "SKU-01", null, null))
        every { prod2.toCompletableFuture() } returns CompletableFuture.completedFuture(Product(2, "SKU-02", null, null))
        every { prod3.toCompletableFuture() } returns CompletableFuture.completedFuture(Product(3, "SKU-03", null, null))

        val result = EXT.target("/inventory/5/withProduct").request().get(object: GenericType<List<InventoryWithProduct>>() {})
        // TODO - warning: the data is sourced from a dump in-memory hashMap so that I could work around the need for a DB.

        val expected = listOf(
                InventoryWithProduct(5,1, 5, "SKU-01"),
                InventoryWithProduct(5,2, 5, "SKU-02"),
                InventoryWithProduct(5,3, 5, "SKU-03"),
        )
        assertEquals(expected, result)
        verify(exactly = 3) { productClient.getProduct(any()) }
    }

    @Test
    fun `POST create inventory`() {
        val newInventory = Inventory(6,1, 1)

        val response: Response = EXT.target("/inventory").request().post(Entity.json(newInventory))
        // TODO - warning: the data is sourced from a dump in-memory hashMap so that I could work around the need for a DB.

        assertEquals(200, response.status)
        val result: Inventory = response.readEntity(object: GenericType<Inventory>() {})
        assertEquals(newInventory, result)
    }

    @Test
    fun `PUT to update inventory`() {
        val updatedInventory = Inventory(5,1, 1)

        val response: Response = EXT.target("/inventory/${updatedInventory.locationId}")
                .request()
                .put(Entity.json(updatedInventory))
        // TODO - warning: the data is sourced from a dump in-memory hashMap so that I could work around the need for a DB.

        assertEquals(200, response.status)
        val result: Inventory = response.readEntity(object: GenericType<Inventory>() {})
        assertEquals(updatedInventory, result)
    }
}