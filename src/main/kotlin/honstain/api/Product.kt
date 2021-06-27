package honstain.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class Product (
    var productId: Long,
    var sku: String,
    var barcode: String?,
    var taxCode: String?,
)