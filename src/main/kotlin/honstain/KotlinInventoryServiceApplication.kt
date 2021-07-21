package honstain

import com.codahale.metrics.MetricFilter
import com.codahale.metrics.graphite.Graphite
import com.codahale.metrics.graphite.GraphiteReporter
import com.fasterxml.jackson.module.kotlin.KotlinModule
import honstain.client.ProductJerseyClient
import honstain.client.ProductJerseyRXClient
import io.dropwizard.Application
import io.dropwizard.client.JerseyClientBuilder
import io.dropwizard.setup.Environment
import java.net.InetSocketAddress
import java.util.*
import java.util.concurrent.TimeUnit
import javax.ws.rs.client.Client
import javax.ws.rs.client.RxInvokerProvider


class KotlinInventoryServiceApplication: Application<KotlinInventoryServiceConfiguration>() {
    companion object {
        @JvmStatic fun main(args: Array<String>) = KotlinInventoryServiceApplication().run(*args)
    }

    override fun getName(): String = "KotlinInventoryService"

    override fun run(config: KotlinInventoryServiceConfiguration, env: Environment) {
        //val uniqueServiceId = 1 //UUID.randomUUID()
        //val graphite = Graphite(InetSocketAddress("localhost", 2003))
        //val reporter = GraphiteReporter.forRegistry(env.metrics())
        //        .prefixedWith("InventoryService.$uniqueServiceId")
        //        .convertRatesTo(TimeUnit.SECONDS)
        //        .convertDurationsTo(TimeUnit.MILLISECONDS)
        //        .filter(MetricFilter.ALL)
        //        .build(graphite)
        //reporter.start(5, TimeUnit.SECONDS)

        /*
        Had some trouble remembering how to interact with the object mapper.
        References:
        * https://github.com/dropwizard/dropwizard/issues/2580
        * https://www.dropwizard.io/en/latest/manual/internals.html
         */
        env.objectMapper.registerModule(KotlinModule())

        //val client: Client = JerseyClientBuilder(env)
        //        .using(config.getJerseyClientConfiguration())
        //        .build(name)
        //val productClient = ProductJerseyClient(client)

        val client1 = JerseyClientBuilder(env)
        val client2 = client1.using(config.getJerseyClientConfiguration())
        val client3: Client = client2.buildRx(name, RxInvokerProvider::class.java)
        val productClient = ProductJerseyRXClient(client3)

        //val httpClient: CloseableHttpClient = HttpClientBuilder(env)
        //        .using(config.getHttpClientConfiguration())
        //        .build(name)
        //val productClient = ProductClient(httpClient, env.objectMapper)

        env.jersey().register(InventoryResource(productClient))
        env.jersey().register(ProvenanceIDFilter())
    }
}
