package dev.scavazzini.clevent.data.webservice

import dev.scavazzini.clevent.data.models.Product
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface ProductService {
    @GET
    suspend fun getProducts(@Url endpoint: String): Response<List<Product>>
}
