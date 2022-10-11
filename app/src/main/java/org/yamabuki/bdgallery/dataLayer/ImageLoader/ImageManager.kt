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
import java.io.InvalidObjectException
import kotlin.math.min
import kotlin.random.Random

class ImageManager(val context: Context, val scope: CoroutineScope, val maxDownloadWorkers: Int = 8) {
    private val runningJobQueue = mutableMapOf<String, Job>()
    private val rLock = Mutex()
    private val stoppedJobQueue = mutableMapOf<String, Job>()
    private val sLock = Mutex()
    private val idleJobQueue = mutableMapOf<String, Job>()
    private val iLock = Mutex()

    private val priorFilenameList = mutableListOf<String>()
    private val pLock = Mutex()

    private val client = OkHttpClient()

    suspend fun dispatcher2(){
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

    suspend fun dispatcher(){
        val uid = Random.nextInt()
        suspend fun runOrResumeJob(filename: String, fromIdle: Boolean){
            /* must run under rLock and iLock or sLock */
            val job: Job
            if (fromIdle) {
                job = this.idleJobQueue[filename]!!
                this.idleJobQueue.remove(filename)
                job.idle = false
            } else {
                job = this.stoppedJobQueue[filename]!!
                this.stoppedJobQueue
            }
            this.runningJobQueue[job.filename] = job
            if (job.started) {
                Log.d("[dispatcher $uid runOrResumeJob]","正在繼續任務 $filename")
                job.controlChan.send(true)

            } else {
                Log.d("[dispatcher $uid runOrResumeJob]","正在新建了任務 $filename")
                job.started = true
                this.scope.launch {
                    imageWorker(job)
                }
                job.controlChan.send(true)
            }
            Log.d("[dispatcher $uid runOrResumeJob]","已啓動任務 $filename")
        }

        suspend fun pauseJob(filename: String){
            /* must run under rLock and sLock */
            val job: Job = this.runningJobQueue[filename]!!
            job.controlChan.send(false)
            this.runningJobQueue.remove(filename)
            this.stoppedJobQueue[filename] = job
            Log.d("[dispatcher $uid pauseJob]","停止了任務 $filename")

        }

        suspend fun idleToStopped(){
            for (job in this.idleJobQueue.values){
                job.idle = false
                this.stoppedJobQueue.put(job.filename, job)
            }
            Log.d("[dispatcher $uid idleToStopped]","移動了 idle 任務到 stop：${this.idleJobQueue.size}")

            this.idleJobQueue.clear()
        }

        val priorJobList = mutableListOf<Job>()
        // todo: 檢查這些 lock！！！
        iLock.lock()
        sLock.lock()
        pLock.lock()

        Log.d("[dispatcher $uid]","started， 最大線程數是 $maxDownloadWorkers")
        for (filename in priorFilenameList){
            val job1 = idleJobQueue.get(filename)
            val job2 = stoppedJobQueue.get(filename)
            if (job1 != null && job2 != null)
                throw InvalidObjectException("[dispatcher] ${job1.filename} 同時出現在兩個列表，過於惡俗！！")
            if (job1 != null) priorJobList.add(job1)
            if (job2 != null) priorJobList.add(job2)
        }
        pLock.unlock()
        priorJobList.sortBy { it.committedTime }
        Log.d("[dispatcher $uid]","${priorJobList.size} 個優先 job")

        if (priorJobList.size == 0){
            sLock.unlock()
            rLock.lock()
            Log.d("[dispatcher $uid]","沒有優先任務，一般派送中，，，")

            while (true){
                if (runningJobQueue.size == maxDownloadWorkers){
                    rLock.unlock()
                    iLock.unlock()
                    Log.d("[dispatcher $uid]","running 數已滿，中止")

                    return
                }
                if (idleJobQueue.isEmpty()){
                    rLock.unlock()
                    iLock.unlock()
                    Log.d("[dispatcher $uid]","idle 隊列已空，中止")

                    return
                }
                val idleList = this.idleJobQueue.values.toList().sortedBy { it.committedTime }
                var newJobCount = this.maxDownloadWorkers - this.runningJobQueue.size
                for (idleJob in idleList){
                    if (newJobCount == 0) break
                    runOrResumeJob(idleJob.filename, true)
                    newJobCount--
                }
                Log.d("[dispatcher $uid]","從 idle 隊列中派送了 ${min(idleList.size, newJobCount)} 個")

            }

        }else{
            rLock.lock()
            Log.d("[dispatcher $uid]","有插隊任務共 ${priorJobList.size} 個")

            if (priorJobList.size > maxDownloadWorkers){
                Log.d("[dispatcher $uid]","插隊任務過多，正在暫停所有 running")

                val runningJobFilenames = this.runningJobQueue.keys.toList()
                for (name in runningJobFilenames){
                    pauseJob(name)
                }
                for (i in 0 until maxDownloadWorkers){
                    val pendingJob: Job = priorJobList[i]
                    runOrResumeJob(pendingJob.filename, pendingJob.idle)
                }
                idleToStopped()
                for (i in maxDownloadWorkers until priorJobList.size){
                    this.idleJobQueue.put(priorJobList[i].filename, priorJobList[i])
                }
            }else{
                if (priorJobList.size > maxDownloadWorkers - this.runningJobQueue.size){
                    val runningList = this.runningJobQueue.values.toList().sortedBy { it.committedTime }
                    val needed: Int = priorJobList.size - maxDownloadWorkers + this.runningJobQueue.size
                    for (i in 0 until needed){
                        pauseJob(runningList[i].filename)
                    }
                    Log.d("[dispatcher $uid]","暫停了 $needed 個執行中任務")

                }
                for (job in priorJobList){
                    runOrResumeJob(job.filename, job.idle)
                }
            }
            rLock.unlock()
            sLock.unlock()
            iLock.unlock()
            Log.d("[dispatcher $uid]","插隊任務派送完畢，中止")
            return
        }

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

    private suspend fun onWorkerComplete(job: Job){
        Log.i("[onWorkerComplete]", job.filename)

        this.rLock.lock()
        runningJobQueue.remove(job.filename)
        this.rLock.unlock()

        job.progressChan.close()//IllegalArgumentException("${job.filename} job 已經完成並退出！！"))
        job.controlChan.close()//IllegalArgumentException("${job.filename} job 已經完成並退出！！"))
        this.dispatcher()
    }

    suspend fun updatePriorList(filenameList: List<String>){
        pLock.lock()
        this.priorFilenameList.clear()
        this.priorFilenameList.addAll(filenameList)
        pLock.unlock()
        this.dispatcher()
    }

    suspend fun commit(filename: String, url: String): Channel<Int> {
        val newJob = Job(filename, url)

        this.iLock.lock()
        idleJobQueue.put(filename, newJob)
        this.iLock.unlock()

        this.dispatcher()
        return newJob.progressChan
    }

    suspend fun imageWorker(job: Job) {
        val filename = job.filename
        val url = job.url
        var running = false  //todo
        val innerChan: Channel<Boolean> = Channel()

        this.scope.launch {
            for (msg in job.controlChan){
                running = msg
                if (msg) innerChan.trySend(true)
            }
            innerChan.close()
        }
        withContext(Dispatchers.IO){
            val file = File(context.cacheDir, filename)
            if (file.exists()){
                job.progressChan.send(101)
                this@ImageManager.onWorkerComplete(job)
                return@withContext
            }
            if (!running) innerChan.receive()

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

            if (!running) innerChan.receive()

            val newFile = File.createTempFile("temp", ".tmp", context.cacheDir) //File(context.cacheDir, filename)
            val outStream = newFile.outputStream()
            try {
                val inBuff = resp.body()?.source()
                if (inBuff == null){
                    job.progressChan.send(-1)
                    this@ImageManager.onWorkerFailed(job)
                    return@withContext
                }
                var total = 0L

                newFile.deleteOnExit()
                val buff = ByteArray(10000)
                while (true){
                    if (!running) innerChan.receive()
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
            }catch (err: IOException){
                outStream.flush()
                outStream.close()
                resp.close()
                job.progressChan.send(-1)
                this@ImageManager.onWorkerFailed(job)
                return@withContext
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
    val controlChan: Channel<Boolean> = Channel(),
    var started: Boolean = false,
    val committedTime: Long = System.currentTimeMillis(),
    var idle: Boolean = true
)

// DNT: 1
// Referer: https://bestdori.com/info/cards/1612/Maya-Yamato-So-Music-Is
// sec-ch-ua: "Microsoft Edge";v="105", " Not;A Brand";v="99", "Chromium";v="105"
// sec-ch-ua-mobile: ?0
// sec-ch-ua-platform: "Windows"
// User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.0.0 Safari/537.36 Edg/105.0.1343.42