package com.example.datastoreexample

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import androidx.datastore.preferences.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

object CustomSerializer: Serializer<Product> {
  override val defaultValue: Product
    get() = Product.getDefaultInstance()

  override suspend fun readFrom(input: InputStream): Product {
    try {
      return Product.parseFrom(input)
    } catch (exception: InvalidProtocolBufferException) {
      throw CorruptionException("Cannot read proto. ", exception)
    }
  }

  override suspend fun writeTo(t: Product, output: OutputStream) {
    t.writeTo(output)
  }
}