package honstain.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import honstain.api.Product
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration


class QuickStartEventConsumer(
        val consumer: Consumer<String?, String?>,
        val productCache: MutableMap<Long, Product>,
        val objectMapper: ObjectMapper
): Runnable {

    val log: Logger = LoggerFactory.getLogger(QuickStartEventConsumer::class.java)

    override fun run() {
        try {
            consumer.subscribe(listOf("quickstart-events"))

            while (true) {
                val records: ConsumerRecords<String?, String?> = consumer.poll(Duration.ofMillis(100))
                if (!records.isEmpty) log.info("Batchsize: ${records.count()}")
                for (record in records) {
                    log.info("${record.topic()} ${record.offset()}, ${record.value()}")
                    val foo: String = record.value()!!
                    val bar = objectMapper.readValue(foo, Product::class.java)
                    productCache[bar.productId] = bar
                    //Thread.sleep(100)
                }
            }
        }
        catch (e: Exception) {
            log.error("Failed to consume message: ${e.message}")
        }
        finally {
            consumer.wakeup()
        }
    }
}