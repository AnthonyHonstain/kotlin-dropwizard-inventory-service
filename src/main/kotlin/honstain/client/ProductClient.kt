package honstain.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import honstain.api.Product
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.CloseableHttpClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import java.net.ConnectException
import java.net.SocketTimeoutException


class ProductClient(val httpClient: CloseableHttpClient, val objectMapper: ObjectMapper) {

    val log: Logger = LoggerFactory.getLogger(ProductClient::class.java)

    fun getProduct(productId: Long): Product {
        val httpGet = HttpGet("http://localhost:7070/product/$productId")

        val provenanceID = MDC.get("ProvenanceID")
        httpGet.addHeader("ProvenanceID", provenanceID)

        try {
            val closeableHttpResponse: CloseableHttpResponse = httpClient.execute(httpGet)

            closeableHttpResponse.use { response ->
                return objectMapper.readValue<Product>(response.entity.content)
            }

        } catch (e: SocketTimeoutException) {
            log.warn("SocketTimeoutException for productId:$productId")
        } catch (e: ConnectException) {
            log.warn("ConnectException for productId:$productId")
        }
        throw Exception("Failed to retrieve data from Product Service")
    }
}