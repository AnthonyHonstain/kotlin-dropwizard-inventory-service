package client

import com.fasterxml.jackson.module.kotlin.KotlinModule
import honstain.api.Product
import honstain.client.ProductJerseyClient
import io.dropwizard.client.JerseyClientBuilder
import io.dropwizard.testing.junit5.DropwizardClientExtension
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.net.URI
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.client.Client
import javax.ws.rs.client.Invocation
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response


@ExtendWith(DropwizardExtensionsSupport::class)
class ProductJerseyClientTest {
    @Path("/product/{productId}")
    class ProductResource {
        @GET
        fun getProduct(@PathParam("productId") productId: Long): Product {
            return Product(1, "SKU-01", null, null)
        }
    }

    val EXT: DropwizardClientExtension = DropwizardClientExtension(ProductResource())

    @Test
    fun `getProduct basic example using Dropwizard stub app`() {
        // TODO - Is there a way around having to continue to register this KotlinModule?
        EXT.environment.objectMapper.registerModule(KotlinModule())

        val client: Client = JerseyClientBuilder(EXT.environment)
                // TODO - skipping the config, just want to get the test going and validate the premise.
                //.using(config.getJerseyClientConfiguration())
                .build(this.toString())

        // Utilized this reference heavily
        //        https://www.dropwizard.io/en/latest/manual/testing.html#testing-client-implementations
        val productClient = ProductJerseyClient(client, EXT.baseUri())
        val expected = Product(1, "SKU-01", null, null)

        val product = productClient.getProduct(1L)
        assertEquals(expected, product)
    }


    // TODO - I was unable to find a reasonable way to mock this client.
    // TODO - it was my aspiration to inject the raw bytes and verify it serialized, but thats probably not the right idea.
    @Disabled
    @Test
    fun `getProduct basic product`() {

        val client = mockk<Client>(relaxed = true)
        val target = mockk<WebTarget>(relaxed = true)
        val invocationBuilder = mockk<Invocation.Builder>(relaxed = true)

        every { invocationBuilder.header(any<String>(), any<String>()) } returns invocationBuilder
        every { target.request() } returns invocationBuilder
        every { client.target(any<String>()) } returns target

        val productClient = ProductJerseyClient(client, URI.create("http://localhost:7070"))

        val productJSON = "{\n" +
            "    \"productId\": 1,\n" +
            "    \"sku\": \"SKU-01\",\n" +
            "    \"barcode\": null,\n" +
            "    \"taxCode\": null\n" +
            "}"
        val response = mockk<Response>()
        every { response.readEntity(Product::class.java) } returns Product(1, "SKU-01", null, null)
        //every { response.entity } returns productJSON.byteInputStream()
        every { invocationBuilder.get() } returns response

        val product = productClient.getProduct(1)
        val expected = Product(1, "SKU-01", null, null)
        assertEquals(expected, product)
    }
}