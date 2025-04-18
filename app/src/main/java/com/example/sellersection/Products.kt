package com.example.sellersection

data class Products(
    var photo: String = "",
    var name: String = "",
    var category: String = "",
    var price: Int = 0,
    var stock: Int = 0,
    var availability: Boolean = false,
    var description: String = ""
    )
