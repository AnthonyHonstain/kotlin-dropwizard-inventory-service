package honstain

import com.fasterxml.jackson.module.kotlin.KotlinModule
import honstain.api.Product
import honstain.client.ProductJerseyRXClient
import honstain.consumer.QuickStartEventConsumer
import io.dropwizard.Application
import io.dropwizard.client.JerseyClientBuilder
import io.dropwizard.kafka.KafkaConsumerBundle
import io.dropwizard.kafka.KafkaConsumerFactory
import io.dropwizard.kafka.KafkaProducerBundle
import io.dropwizard.kafka.KafkaProducerFactory
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import io.dropwizard.util.Duration
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.internals.NoOpConsumerRebalanceListener
import org.apache.kafka.clients.producer.Producer
import java.util.concurrent.ExecutorService
import javax.ws.rs.client.Client
import javax.ws.rs.client.RxInvokerProvider


open class KotlinInventoryServiceApplication: Application<KotlinInventoryServiceConfiguration>() {
    companion object {
        @JvmStatic fun main(args: Array<String>) = KotlinInventoryServiceApplication().run(*args)
    }

    override fun getName(): String = "KotlinInventoryService"

    override fun initialize(bootstrap: Bootstrap<KotlinInventoryServiceConfiguration>) {
        super.initialize(bootstrap)

        bootstrap.addBundle(kafkaConsumer)
        bootstrap.addBundle(kafkaDLTConsumer)
        bootstrap.addBundle(kafkaProducer)
    }

    override fun run(config: KotlinInventoryServiceConfiguration, env: Environment) {
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
        //val productClient = ProductJerseyClient(client, URI.create("http://localhost:7070"))

        val client1 = JerseyClientBuilder(env)
        val client2 = client1.using(config.getJerseyClientConfiguration())
        val client3: Client = client2.buildRx(name, RxInvokerProvider::class.java)
        val productClient = ProductJerseyRXClient(client3, env.metrics())

        //val httpClient: CloseableHttpClient = HttpClientBuilder(env)
        //        .using(config.getHttpClientConfiguration())
        //        .build(name)
        //val productClient = ProductClient(httpClient, env.objectMapper)

        val productCache = mutableMapOf<Long, Product>()

        val dltConsumer: Consumer<String?, String?> = kafkaDLTConsumer.consumer
        dltConsumer.subscribe(listOf("dlq-product"))

        env.jersey().register(InventoryResource(productClient, productCache, dltConsumer, env.objectMapper))
        env.jersey().register(ProvenanceIDFilter())

        val consumer: Consumer<String?, String?> = kafkaConsumer.consumer
        val producer: Producer<String?, String?> = kafkaProducer.producer
        val quickStartEventConsumer = QuickStartEventConsumer(consumer, producer, productCache, env.objectMapper, env.metrics())
        val executorService: ExecutorService = env.lifecycle()
                .executorService("Kafka-quickstart-event-consumer")
                .maxThreads(1)
                .shutdownTime(Duration.seconds(1))
                .build()
        executorService.submit(quickStartEventConsumer)

        println("Dropwizard lifecycle tracking the following managedObjects:")
        for (lifecycle in env.lifecycle().managedObjects) {
            println("ManagedObject: $lifecycle")
        }
    }

    private val kafkaConsumer: KafkaConsumerBundle<String?, String?, KotlinInventoryServiceConfiguration> =
            object : KafkaConsumerBundle<String?, String?, KotlinInventoryServiceConfiguration>(emptyList<String>(), NoOpConsumerRebalanceListener()) {

                override fun getKafkaConsumerFactory(configuration: KotlinInventoryServiceConfiguration): KafkaConsumerFactory<String?, String?> {
                    return configuration.getKafkaConsumerFactory()!!
                }
            }

    private val kafkaDLTConsumer: KafkaConsumerBundle<String?, String?, KotlinInventoryServiceConfiguration> =
            object : KafkaConsumerBundle<String?, String?, KotlinInventoryServiceConfiguration>(emptyList<String>(), NoOpConsumerRebalanceListener()) {

                override fun getKafkaConsumerFactory(configuration: KotlinInventoryServiceConfiguration): KafkaConsumerFactory<String?, String?> {
                    return configuration.getKafkaDLTConsumerFactory()!!
                }
            }

    private val kafkaProducer: KafkaProducerBundle<String?, String?, KotlinInventoryServiceConfiguration> =
            object : KafkaProducerBundle<String?, String?, KotlinInventoryServiceConfiguration>(emptyList<String>()) {

                override fun getKafkaProducerFactory(configuration: KotlinInventoryServiceConfiguration): KafkaProducerFactory<String?, String?> {
                    return configuration.getKafkaProducerFactory()!!
                }
            }
}
