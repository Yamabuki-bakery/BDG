package org.yamabuki.bdgallery.dataLayer.retrofit

import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET


private val doriBase = "https://bestdori.com"

private val retrofit = Retrofit.Builder().addConverterFactory(ScalarsConverterFactory.create())
    .baseUrl(doriBase).build()

// https://developer.android.com/codelabs/basic-android-kotlin-training-getting-data-internet?hl=en#5

interface DoriServices {
    @GET("/api/cards/all.5.json")
    suspend fun getCards(): String

    @GET("/api/comics/all.5.json")
    suspend fun getAllManga(): String
}

object Dori {
    val retrofitService : DoriServices by lazy {
        retrofit.create(DoriServices::class.java)
    }
}