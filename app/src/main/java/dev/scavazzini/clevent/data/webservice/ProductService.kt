package dev.scavazzini.clevent.data.webservice

import dev.scavazzini.clevent.data.models.Product
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url

interface ProductService {
    @GET
    suspend fun getProducts(@Url endpoint: String): Response<List<Product>>

    companion object {
        private const val BASE_URL = "https://raw.githubusercontent.com/scavazzini/clevent/main/app/src/main/assets/mocks/"

        fun create(): ProductService = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ProductService::class.java)
    }
}
