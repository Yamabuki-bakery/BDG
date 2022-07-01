package org.yamabuki.bdgallery.dataLayer.retrofit

import org.yamabuki.bdgallery.R
import org.yamabuki.bdgallery.dataType.ServerArea
import org.yamabuki.bdgallery.ugly.MyStrings
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path


private val doriBase = MyStrings.get(R.string.dori_webroot)

private val retrofit = Retrofit.Builder().addConverterFactory(ScalarsConverterFactory.create())
    .baseUrl(doriBase).build()

// https://developer.android.com/codelabs/basic-android-kotlin-training-getting-data-internet?hl=en#5

interface DoriServices {
    @GET("/api/cards/all.5.json")
    suspend fun getCards(): String

    @GET("/api/comics/all.5.json")
    suspend fun getAllManga(): String

    @GET("/api/explorer/{serverArea}/assets/stamp/01.json")
    suspend fun _getAllStickers(@Path("serverArea") serverAreaStr: String): String
//
//    suspend fun getAllStickers(serverArea: ServerArea): String {
//        return _getAllStickers(serverArea.lower)
//    }
}

object Dori {
    val mService : DoriServices by lazy {
        retrofit.create(DoriServices::class.java)
    }
}