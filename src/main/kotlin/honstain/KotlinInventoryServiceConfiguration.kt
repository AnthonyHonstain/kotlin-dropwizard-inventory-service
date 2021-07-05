package honstain


import com.fasterxml.jackson.annotation.JsonProperty
import io.dropwizard.Configuration
import io.dropwizard.client.HttpClientConfiguration
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
}