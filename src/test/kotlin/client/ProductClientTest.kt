package client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import honstain.api.Product
import honstain.client.ProductClient
import io.mockk.every
import io.mockk.mockk
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.impl.client.CloseableHttpClient
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ProductClientTest {

    @Test
    fun `getProduct basic product`() {
        val httpClient = mockk<CloseableHttpClient>(relaxed = true)
        val objectMapper = ObjectMapper().registerModule(KotlinModule())
        val productClient = ProductClient(httpClient, objectMapper)

        val productJSON = "{\n" +
            "    \"productId\": 1,\n" +
            "    \"sku\": \"SKU-01\",\n" +
            "    \"barcode\": null,\n" +
            "    \"taxCode\": null\n" +
            "}"
        val closeableHttpResponse = mockk<CloseableHttpResponse>(relaxed = true)
        every { closeableHttpResponse.entity.content } returns productJSON.byteInputStream()
        every { httpClient.execute(any()) } returns closeableHttpResponse

        val product = productClient.getProduct(1)
        val expected = Product(1, "SKU-01", null, null)
        assertEquals(expected, product)
    }
}