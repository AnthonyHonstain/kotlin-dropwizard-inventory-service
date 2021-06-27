import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import honstain.InventoryResource
import honstain.api.Inventory
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport
import io.dropwizard.testing.junit5.ResourceExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import javax.ws.rs.client.Entity
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response


@ExtendWith(DropwizardExtensionsSupport::class)
class InventoryResourceTest {
    /*
    Modeling this test based on the example in the official docs Dropwizard v2.0.23:
    https://www.dropwizard.io/en/latest/manual/testing.html#testing-resources
     */

    val EXT: ResourceExtension = ResourceExtension.builder()
            .addResource(InventoryResource())
            .setMapper(ObjectMapper().registerModule(KotlinModule()))
            .build()

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