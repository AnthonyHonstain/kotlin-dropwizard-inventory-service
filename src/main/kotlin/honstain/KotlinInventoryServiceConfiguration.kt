package honstain


import com.fasterxml.jackson.annotation.JsonProperty
import io.dropwizard.Configuration
import io.dropwizard.client.HttpClientConfiguration
import io.dropwizard.client.JerseyClientConfiguration
import io.dropwizard.kafka.KafkaConsumerFactory
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
}