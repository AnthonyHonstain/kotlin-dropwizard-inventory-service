package honstain


import com.fasterxml.jackson.annotation.JsonProperty
import io.dropwizard.Configuration
import io.dropwizard.client.HttpClientConfiguration
import io.dropwizard.client.JerseyClientConfiguration
import io.dropwizard.kafka.KafkaConsumerFactory
import io.dropwizard.kafka.KafkaProducerFactory
import javax.validation.Valid
import javax.validation.constraints.NotNull


class KotlinInventoryServiceConfiguration : Configuration() {

    @Valid
    @NotNull
    var httpClient = HttpClientConfiguration()

    @JsonProperty("httpClient")
    fun getHttpClientConfiguration(): HttpClientConfiguration { return this.httpClient }

    @JsonProperty("httpClient")
    fun setHttpClientConfiguration(httpClient: HttpClientConfiguration) {
        this.httpClient = httpClient
    }

    private var jerseyClient: @Valid @NotNull JerseyClientConfiguration? = JerseyClientConfiguration()

    @JsonProperty("jerseyClient")
    fun getJerseyClientConfiguration(): JerseyClientConfiguration? {
        return jerseyClient
    }

    @JsonProperty("jerseyClient")
    fun setJerseyClientConfiguration(jerseyClient: JerseyClientConfiguration?) {
        this.jerseyClient = jerseyClient
    }

    private var kafkaConsumerFactory: @Valid @NotNull KafkaConsumerFactory<String?, String?>? = null

    @JsonProperty("consumer")
    fun getKafkaConsumerFactory(): KafkaConsumerFactory<String?, String?>? {
        return kafkaConsumerFactory
    }

    @JsonProperty("consumer")
    fun setKafkaConsumerFactory(consumerFactory: KafkaConsumerFactory<String?, String?>) {
        this.kafkaConsumerFactory = consumerFactory
    }

    private var kafkaDLTConsumerFactory: @Valid @NotNull KafkaConsumerFactory<String?, String?>? = null

    @JsonProperty("dltConsumer")
    fun getKafkaDLTConsumerFactory(): KafkaConsumerFactory<String?, String?>? {
        return kafkaDLTConsumerFactory
    }

    @JsonProperty("dltConsumer")
    fun setKafkaDLTConsumerFactory(consumerFactory: KafkaConsumerFactory<String?, String?>) {
        this.kafkaDLTConsumerFactory = consumerFactory
    }

    private var kafkaProducerFactory: @Valid @NotNull KafkaProducerFactory<String?, String?>? = null

    @JsonProperty("dltProducer")
    fun getKafkaProducerFactory(): KafkaProducerFactory<String?, String?>? {
        return kafkaProducerFactory
    }

    @JsonProperty("dltProducer")
    fun setKafkaProducerFactory(producerFactory: KafkaProducerFactory<String?, String?>) {
        this.kafkaProducerFactory = producerFactory
    }
}