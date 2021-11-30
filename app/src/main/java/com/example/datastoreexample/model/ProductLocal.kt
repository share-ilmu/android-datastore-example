package com.example.datastoreexample.model

data class ProductLocal(
  val id: String? = null,
  val name: String? = null,
  val image: String? = null,
  val quantity: Int? = null,
  val price: Long? = null,
  val addOn: AddOnLocal? = null
)
