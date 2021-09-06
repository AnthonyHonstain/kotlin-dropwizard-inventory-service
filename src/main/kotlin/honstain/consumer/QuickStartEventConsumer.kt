package honstain.consumer

import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.MetricRegistry.name
import com.codahale.metrics.Timer
import com.fasterxml.jackson.databind.ObjectMapper
import honstain.api.Product
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration


class QuickStartEventConsumer(
        val consumer: Consumer<String?, String?>,
        val producer: Producer<String?, String?>,
        val productCache: MutableMap<Long, Product>,
        val objectMapper: ObjectMapper,
        metrics: MetricRegistry,
): Runnable {

    val log: Logger = LoggerFactory.getLogger(QuickStartEventConsumer::class.java)

    val productTimer: Timer = metrics.timer(name(QuickStartEventConsumer::class.java, "productTimer"))
    val productBatchTimer: Timer = metrics.timer(name(QuickStartEventConsumer::class.java, "productBatchTimer"))

    override fun run() {
        try {
            consumer.subscribe(listOf("quickstart-events"))

            while (true) {
                val batchRunningTimer = productBatchTimer.time()

                val records: ConsumerRecords<String?, String?> = consumer.poll(Duration.ofMillis(100))

                // A couple of options here for logging batchsize as I wanted to confirm
                // the consumer configuration setting were having the expected impact.
                if (!records.isEmpty) log.info("Batchsize: ${records.count()}")
                //log.info("Batchsize: ${records.count()}")

                for (record in records) {
                    val productRunningTimer = productTimer.time()
                    log.info("${record.topic()} ${record.offset()}, ${record.value()}")
                    val foo: String = record.value()!!
                    val bar = objectMapper.readValue(foo, Product::class.java)

                    // In order to observe some synthetic delay's in processing, I introduced this
                    // logic to conditionally DLQ some products, it will also take longer to process
                    // the ones that fail and are sent to the DLQ.
                    if (bar.productId % 50 == 0L) {
                        // I realize this is kind of confusing, as this consumer doesn't really have that much to do,
                        // but I am forcing the DLQ to happen as if some sort of external service call had failed.
                        //   Just avoiding actually doing a service call here for simplicity while I experiment.

                        // THIS IS WHERE WE PRETEND SOME SORT OF ERROR / TIMEOUT HAPPENED
                        Thread.sleep(50)

                        producer.send(ProducerRecord("dlq-product", foo))
                    } else {
                        // Sleeping/delay for less time on the happy path.
                        Thread.sleep(10)

                        productCache[bar.productId] = bar
                    }
                    productRunningTimer.stop()
                }
                batchRunningTimer.stop()
            }
        }
        catch (e: Exception) {
            log.error("Failed to process records - exception: $e message: ${e.message}")
        }
        finally {
            consumer.wakeup()
        }
    }
}