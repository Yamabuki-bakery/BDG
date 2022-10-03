package org.yamabuki.bdgallery.dataLayer.ImageLoader

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.yamabuki.bdgallery.R
import java.io.BufferedOutputStream
import java.io.File
import java.io.IOException
import java.io.OutputStream

class ImageManager(val context: Context, val maxDownloadWorkers: Int = 8) {
    private val jobQueue = mutableListOf<Int>()
    private val client = OkHttpClient()

    fun getCacheDir(): File{
        return context.cacheDir
    }

    fun checkImage(filename: String, url: String): Flow<Int> = flow {
        val file = File(context.cacheDir, filename)
        if (file.exists()){
            emit(101)
            return@flow
        }

        val fullUrl = context.getString(R.string.dori_webroot) + url
        val request = Request.Builder().url(fullUrl)
            .header("DHT", "!")
            .header("Referer", "https://bestdori.com/")
            .header("sec-ch-ua", "\"Microsoft Edge\";v=\"105\", \" Not;A Brand\";v=\"99\", \"Chromium\";v=\"105\"")
            .header("sec-ch-ua-mobile", "?0")
            .header("sec-ch-ua-platform", "\"Windows\"")
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.0.0 Safari/537.36 Edg/105.0.1343.42")
            .get()
            .build()

        val resp: okhttp3.Response
        try {
            resp = client.newCall(request).execute()
        }catch (err: IOException){
            Log.d("[checkImage]", "File: $filename, URL: $url, 網路過於惡俗")
            emit(-1)
            return@flow
        }

        if (!resp.isSuccessful){
            Log.d("[checkImage]", "File: $filename, URL: $url, request failed!")
            emit(-1)
            return@flow
        }
        Log.d("[checkImage]", "$filename  downloading!")
        val contentLength = resp.header("content-length")
        var filesize = 0L
        if (contentLength != null){
            filesize = contentLength.toLong()
            Log.d("[checkImage]", "$filename size is $filesize!!")
        }

        val inBuff = resp.body()?.byteStream()
        if (inBuff == null){
            emit(-1)
            return@flow
        }
        var total = 0L

        val newFile = File.createTempFile("temp", ".tmp", context.cacheDir) //File(context.cacheDir, filename)
        val outStream = newFile.outputStream()
        newFile.deleteOnExit()
        val buff = ByteArray(10000)
        while (true){
            val byte = inBuff.read(buff)
            if ((byte == -1) or (total == filesize)) {
                break
            }
            //Log.d("[checkImage]", "$filename read $byte data!!")
            outStream.write(buff, 0, byte)
            total += byte
            if (filesize != 0L)  emit(((total.toFloat()/filesize)*100).toInt())
        }

        outStream.flush()
        outStream.close()
        newFile.renameTo(File(context.cacheDir, filename))
        emit(101)
        Log.d("[checkImage]", "$filename download OK!")
    }
}
// DNT: 1
// Referer: https://bestdori.com/info/cards/1612/Maya-Yamato-So-Music-Is
// sec-ch-ua: "Microsoft Edge";v="105", " Not;A Brand";v="99", "Chromium";v="105"
// sec-ch-ua-mobile: ?0
// sec-ch-ua-platform: "Windows"
// User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.0.0 Safari/537.36 Edg/105.0.1343.42