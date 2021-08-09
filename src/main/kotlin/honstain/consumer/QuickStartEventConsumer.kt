package honstain.consumer

import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerRecords
import java.time.Duration


class QuickStartEventConsumer(val consumer: Consumer<String?, String?>): Runnable {

    override fun run() {
        try {
            consumer.subscribe(listOf("quickstart-events"))

            while (true) {
                val records: ConsumerRecords<String?, String?> = consumer.poll(Duration.ofMillis(100))
                for (record in records) {
                    println("${record.topic()} ${record.offset()}, ${record.value()}")
                }
            }
        }
        finally {
            consumer.wakeup()
        }
    }
}