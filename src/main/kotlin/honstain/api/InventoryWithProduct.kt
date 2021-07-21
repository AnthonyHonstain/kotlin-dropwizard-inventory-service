package honstain.api

data class InventoryWithProduct(
        var locationId: Long,
        var productId: Long,
        var quantity: Int,
        var sku: String?,
)