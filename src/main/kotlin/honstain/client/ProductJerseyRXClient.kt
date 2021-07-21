package honstain.client

import honstain.api.Product
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import java.util.concurrent.CompletionStage
import javax.ws.rs.client.Client
import javax.ws.rs.client.Invocation
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType

class ProductJerseyRXClient(val client: Client) {

    val log: Logger = LoggerFactory.getLogger(ProductJerseyRXClient::class.java)

    val ProductServiceTarget: WebTarget = client.target("http://localhost:7070")

    fun getProduct(productId: Long): CompletionStage<Product> {
        val provenanceID = MDC.get("ProvenanceID")

        log.debug("ProductClient $productId")

        val getProductTarget: WebTarget = ProductServiceTarget
                .path("product").path(productId.toString())

        val invocation: Invocation.Builder = getProductTarget
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("ProvenanceID", provenanceID)

        val response: CompletionStage<Product> = invocation.rx().get(Product::class.java)
                .exceptionally { throwable ->
                    log.debug("Error happened ${throwable.message}")
                    Product(productId, "ERROR", null, null)
                }

        return response
    }
}