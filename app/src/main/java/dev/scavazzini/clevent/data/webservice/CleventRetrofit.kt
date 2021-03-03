package dev.scavazzini.clevent.data.webservice

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object CleventRetrofit {

    private val retrofit by lazy {
        Retrofit.Builder()
                .baseUrl("https://raw.githubusercontent.com/scavazzini/clevent/main/app/src/main/assets/mocks/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
    }

    val productService: ProductService by lazy {
        retrofit.create(ProductService::class.java)
    }

}
