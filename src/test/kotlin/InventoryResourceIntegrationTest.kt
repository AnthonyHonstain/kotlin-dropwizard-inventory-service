import honstain.KotlinInventoryServiceApplication
import honstain.KotlinInventoryServiceConfiguration
import honstain.api.Inventory
import io.dropwizard.testing.ResourceHelpers
import io.dropwizard.testing.junit5.DropwizardAppExtension
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.extension.ExtendWith
import javax.ws.rs.client.Client
import javax.ws.rs.client.Entity
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response


@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@ExtendWith(DropwizardExtensionsSupport::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InventoryResourceIntegrationTest {
    /*
    Modeling this test based on the example in the official docs Dropwizard v2.0.23:
    https://www.dropwizard.io/en/latest/manual/testing.html#integration-testing

    These tests stand up an entire APPLICATION and hit it with REAL HTTP requests.
     */

    @Test
    @Order(1)
    fun `GET single inventory`() {
        val client: Client = EXT.client()
        val result: List<Inventory> = client.target("http://localhost:${EXT.localPort}/inventory/5")
                .request()
                .get(object : GenericType<List<Inventory>>() {})
        // TODO - warning: the data is sourced from a dump in-memory hashMap so that I could work around the need for a DB.
        val expected = listOf(
                Inventory(5, 1, 5),
                Inventory(5, 2, 5),
                Inventory(5, 3, 5),
        )
        assertEquals(expected, result)
    }

    @Test
    @Order(2)
    fun `GET all inventory`() {
        val client: Client = EXT.client()
        val result: List<Inventory> = client.target("http://localhost:${EXT.localPort}/inventory")
                .request()
                .get(object : GenericType<List<Inventory>>() {})
        // TODO - warning: the data is sourced from a dump in-memory hashMap so that I could work around the need for a DB.
        val expected = listOf(
                Inventory(5, 1, 5),
                Inventory(5, 2, 5),
                Inventory(5, 3, 5),
                Inventory(6, 4, 1),
                Inventory(6, 5, 1),
        )
        assertEquals(expected, result)
    }

    @Test
    @Order(3)
    fun `POST to create inventory`() {
        val newInventory = Inventory(6, 4, 1)

        val client: Client = EXT.client()
        val response: Response = client.target("http://localhost:${EXT.localPort}/inventory")
                .request()
                .post(Entity.json(newInventory))
        // TODO - warning: the data is sourced from a dump in-memory hashMap so that I could work around the need for a DB.

        assertEquals(200, response.status)
        val result: Inventory = response.readEntity(object : GenericType<Inventory>() {})
        assertEquals(newInventory, result)
    }

    @Test
    @Order(4)
    fun `PUT to update inventory`() {
        val updatedInventory = Inventory(5, 1, 1)

        val client: Client = EXT.client()
        val response: Response = client.target("http://localhost:${EXT.localPort}/inventory/${updatedInventory.locationId}")
                .request()
                .put(Entity.json(updatedInventory))
        // TODO - warning: the data is sourced from a dump in-memory hashMap so that I could work around the need for a DB.

        assertEquals(200, response.status)
        val result: Inventory = response.readEntity(object : GenericType<Inventory>() {})
        assertEquals(updatedInventory, result)
    }

    @Test
    @Order(5)
    fun `PUT to update inventory with mismatch body and path param`() {
        val updatedInventory =  Inventory(5, 1, 1)

        val client: Client = EXT.client()
        val response: Response = client.target("http://localhost:${EXT.localPort}/inventory/1")
                .request()
                .put(Entity.json(updatedInventory))
        // TODO - warning: the data is sourced from a dump in-memory hashMap so that I could work around the need for a DB.

        assertEquals(400, response.status)
    }

    companion object {
        @JvmStatic
        private val EXT: DropwizardAppExtension<KotlinInventoryServiceConfiguration> = DropwizardAppExtension(
                KotlinInventoryServiceApplication::class.java,
                ResourceHelpers.resourceFilePath("inventory-service-test-config.yml")
        )
    }
}