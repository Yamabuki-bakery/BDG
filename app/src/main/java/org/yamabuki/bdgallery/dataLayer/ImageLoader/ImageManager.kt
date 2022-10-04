package org.yamabuki.bdgallery.dataLayer.ImageLoader

import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import okhttp3.OkHttpClient
import okhttp3.Request
import org.yamabuki.bdgallery.R
import java.io.File
import java.io.IOException
import java.nio.channels.ClosedChannelException
import kotlin.random.Random

class ImageManager(val context: Context, val scope: CoroutineScope, val maxDownloadWorkers: Int = 8) {
    private val runningJobQueue = mutableMapOf<String, Job>()
    private val rLock = Mutex()
    private val stoppedJobQueue = mutableMapOf<String, Job>()
    private val sLock = Mutex()
    private val idleJobQueue = mutableMapOf<String, Job>()

    private val priorJobList = mutableListOf<String>()
    private val pLock = Mutex()

    private val client = OkHttpClient()

    suspend fun dispatcher(){
        val toRemove = mutableListOf<String>()

        this.sLock.lock()
        this.rLock.lock()
        for (record in stoppedJobQueue){
            val job = record.value
            this.runningJobQueue.put(record.key, job)
            toRemove.add(record.key)
        }
        for (record in toRemove){
            stoppedJobQueue.remove(record)
            val job: Job = this@ImageManager.runningJobQueue.get(record)!!
            this.scope.launch {
                imageWorker(job)
            }
            Log.i("[dispatcher]", record)
        }
        this.rLock.unlock()
        this.sLock.unlock()

    }

    suspend fun onWorkerFailed(job: Job){
        Log.e("[onWorkerFailed]", job.filename)

        this.rLock.lock()
        runningJobQueue.remove(job.filename)
        this.rLock.unlock()

        this.dispatcher()
        this.scope.launch {
            delay(1000)

            this@ImageManager.sLock.lock()
            stoppedJobQueue.put(job.filename, job)
            this@ImageManager.sLock.unlock()

            this@ImageManager.dispatcher()
        }
    }

    suspend fun onWorkerComplete(job: Job){
        Log.i("[onWorkerComplete]", job.filename)

        this.rLock.lock()
        runningJobQueue.remove(job.filename)
        this.rLock.unlock()

        job.progressChan.close()//IllegalArgumentException("${job.filename} job 已經完成並退出！！"))
        job.controlChan.close()//IllegalArgumentException("${job.filename} job 已經完成並退出！！"))
        this.dispatcher()
    }

    fun updatePriorList(filenameList: List<String>){
        this.priorJobList.clear()
        this.priorJobList.addAll(filenameList)
    }

    suspend fun commit(filename: String, url: String): Channel<Int> {
        val newJob = Job(filename, url)

        this.sLock.lock()
        stoppedJobQueue.put(filename, newJob)
        this.sLock.unlock()

        this.dispatcher()
        return newJob.progressChan
    }

    suspend fun imageWorker(job: Job) {
        val filename = job.filename
        val url = job.url
        var running = false  //todo

        withContext(Dispatchers.IO){
            val file = File(context.cacheDir, filename)
            if (file.exists()){
                job.progressChan.send(101)
                this@ImageManager.onWorkerComplete(job)
                return@withContext
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
                //Log.e("[checkImage]", "File: $filename, URL: $url, 網路過於惡俗")
                job.progressChan.send(-1)
                this@ImageManager.onWorkerFailed(job)
                return@withContext
            }

            if (!resp.isSuccessful){
                //Log.e("[checkImage]", "File: $filename, URL: $url, request failed!")
                job.progressChan.send(-1)
                this@ImageManager.onWorkerFailed(job)
                return@withContext
            }
            //Log.d("[checkImage]", "$filename downloading!")
            val contentLength = resp.header("content-length")
            var filesize = 0L
            if (contentLength != null){
                filesize = contentLength.toLong()
                //Log.d("[checkImage]", "$filename size is $filesize!!")
            }

            val inBuff = resp.body()?.source()
            if (inBuff == null){
                job.progressChan.send(-1)
                this@ImageManager.onWorkerFailed(job)
                return@withContext
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
                if (filesize != 0L) {
                    job.progressChan.send(((total.toFloat()/filesize)*100).toInt())
                }
            }

            outStream.flush()
            outStream.close()
            resp.close()
            newFile.renameTo(File(context.cacheDir, filename))
            job.progressChan.send(101)
            this@ImageManager.onWorkerComplete(job)
            //Log.d("[checkImage]", "$filename download OK!")
        }
    }
    fun getCacheDir(): File{
        return context.cacheDir
    }
}

data class Job(
    val filename: String,
    val url: String,
    val id: Int = Random.nextInt(),
    val progressChan: Channel<Int> = Channel(2),
    val controlChan: Channel<Boolean> = Channel(2)
)

// DNT: 1
// Referer: https://bestdori.com/info/cards/1612/Maya-Yamato-So-Music-Is
// sec-ch-ua: "Microsoft Edge";v="105", " Not;A Brand";v="99", "Chromium";v="105"
// sec-ch-ua-mobile: ?0
// sec-ch-ua-platform: "Windows"
// User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.0.0 Safari/537.36 Edg/105.0.1343.42