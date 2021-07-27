package honstain.client

import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.MetricRegistry.name
import com.codahale.metrics.Timer
import honstain.api.Product
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import java.util.concurrent.CompletionStage
import javax.ws.rs.client.Client
import javax.ws.rs.client.Invocation
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType

class ProductJerseyRXClient(val client: Client, val metrics: MetricRegistry) {

    val callTimer: Timer = metrics.timer(name(ProductJerseyRXClient::class.java, "callTimer"))

    val log: Logger = LoggerFactory.getLogger(ProductJerseyRXClient::class.java)

    val ProductServiceTarget: WebTarget = client.target("http://localhost:7070")

    fun getProduct(productId: Long): CompletionStage<Product> {
        val provenanceID = MDC.get("ProvenanceID")
        val start = callTimer.time()
        log.debug("ProductClient $productId with provenanceID:$provenanceID")

        val getProductTarget: WebTarget = ProductServiceTarget
                .path("product").path(productId.toString())

        val invocation: Invocation.Builder = getProductTarget
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("ProvenanceID", provenanceID)

        val response: CompletionStage<Product> = invocation.rx().get(Product::class.java)
                .whenComplete { complete, throwableThing ->
                    start.stop()
                }
                .exceptionally { throwable ->
                    log.debug("Error happened ${throwable.message}")
                    Product(productId, "ERROR", null, null)
                }

        return response
    }
}