package honstain.client

import honstain.api.Product

interface IProduct {
    fun getProduct(productId: Long): Product
}