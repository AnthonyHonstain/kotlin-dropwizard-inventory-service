package honstain.client

import honstain.api.Product
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.URI
import java.net.URL
import javax.ws.rs.ProcessingException
import javax.ws.rs.client.Client
import javax.ws.rs.client.Invocation
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

class ProductJerseyClient(val client: Client, uri: URI) : IProduct {

    val log: Logger = LoggerFactory.getLogger(ProductJerseyClient::class.java)

    val ProductServiceTarget: WebTarget = client.target(uri) //client.target("http://localhost:7070")

    override fun getProduct(productId: Long): Product {
        val provenanceID = MDC.get("ProvenanceID")

        val getProductTarget: WebTarget = ProductServiceTarget.path("product").path(productId.toString())
        val invocation: Invocation.Builder = getProductTarget
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("ProvenanceID", provenanceID)

        try {
            val response: Response = invocation.get()

            val result: Product = response.readEntity(Product::class.java)
            return result
        } catch (e: ProcessingException) {
            log.warn(e.localizedMessage)
            if (e.cause is SocketTimeoutException) {
                log.warn("SocketTimeoutException for productId:$productId")
            } else if (e.cause is ConnectException) {
                log.warn("ConnectException for productId: $productId")
            }
        }
        return Product(-1, "STUB-error-default", null, null)
    }
}