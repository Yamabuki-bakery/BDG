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

    private val eventChannel = Channel<EventMessage>()

    private val client = OkHttpClient()

    init {
        scope.launch { dispatcher2() }
    }

    suspend fun dispatcher2(){
        var event: EventMessage
        val dLock = Mutex()
        while (true){
            event = this.eventChannel.receive()
            Log.d("[dispatcher2 EVENT]", event.type.name)
            when(event.type){
                EventMessageType.WORKER_COMPLETE -> {
                    val job = event.job!!


                    this.rLock.lock()
                    runningJobQueue.remove(job.filename)
                    this.rLock.unlock()

                    job.progressChan.close()//IllegalArgumentException("${job.filename} job 已經完成並退出！！"))
                    job.controlChan.close()//IllegalArgumentException("${job.filename} job 已經完成並退出！！"))
                    Log.i("[dispatcher2 onWorkerComplete]", job.filename)
                }
                EventMessageType.WORKER_FAILED -> {
                    val job = event.job!!


                    this.rLock.lock()
                    runningJobQueue.remove(job.filename)
                    this.rLock.unlock()
                    Log.w("[dispatcher2 onWorkerFailed]", job.filename)
                    job.started = false  // need to restart

                    this.scope.launch {
                        delay(1000)

                        this@ImageManager.sLock.lock()
                        stoppedJobQueue.put(job.filename, job)
                        this@ImageManager.sLock.unlock()

                        eventChannel.send(EventMessage(EventMessageType.JUST_RERUN))
                    }
                    continue
                }
                EventMessageType.WORKER_PAUSED -> {
                    val job = event.job!!

                    rLock.lock()
                    sLock.lock()
                    this.runningJobQueue.remove(job.filename)
                    this.stoppedJobQueue[job.filename] = job
                    sLock.unlock()
                    rLock.unlock()
                    Log.d("[dispatcher2 pauseJob]","暫停了任務 ${job.filename}")
                    //continue
                }
                EventMessageType.WORKER_RESUMED -> {
                    val job = event.job!!

                    rLock.lock()
                    this.runningJobQueue[job.filename] = job
                    rLock.unlock()

                    Log.d("[dispatcher2 runOrResumeJob]","繼續了任務 ${job.filename}")
                    continue
                }
                EventMessageType.WORKER_STARTED -> {
                    val job = event.job!!

                    rLock.lock()
                    this.runningJobQueue[job.filename] = job
                    rLock.unlock()
                    Log.d("[dispatcher2 runOrResumeJob]","任務已啓動 ${job.filename}")
                    continue
                }
                EventMessageType.JOB_COMMITED -> {
                    val job = event.job!!

                    this.iLock.lock()
                    idleJobQueue.put(job.filename, job)
                    this.iLock.unlock()
                    Log.d("[dispatcher2 JOB_COMMITED]", job.filename)
                }
                EventMessageType.JOB_PRIORITY_CHANGE -> {
                    Log.d("[dispatcher2 JOB_PRIORITY_CHANGE]", "JOB_PRIORITY_CHANGE")
                }
                EventMessageType.JUST_RERUN -> {
                    Log.d("[dispatcher2 JUST_RERUN]", "JUST_RERUN")
                }
            }

            if (dLock.tryLock()){
                scope.launch {
                    dispatcher()
                    Log.v("[dispatcher2 EVENT]", "event ${event.type.name} is finished")
                    delay(500)
                    dLock.unlock()
                }
            }

        }
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
                this.stoppedJobQueue.remove(filename)
            }

            if (job.started) {
                job.controlChan.send(true)

            } else {

                job.started = true
                this.scope.launch {
                    imageWorker(job)
                }
                job.controlChan.send(true)
            }
            //Log.d("[dispatcher $uid runOrResumeJob]","已啓動任務 $filename")
        }

        suspend fun pauseJob(filename: String){
            /* must run under rLock and sLock */
            val job: Job = this.runningJobQueue[filename]!!
            job.controlChan.send(false)
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

        //Log.d("[dispatcher $uid]","started， 最大線程數是 $maxDownloadWorkers")
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
        //Log.d("[dispatcher $uid]","${priorJobList.size} 個優先 job")

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
                Log.d("[dispatcher $uid]","從 idle 隊列中派送了 ${min(idleList.size, newJobCount)} 個，dispatch 停止")

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
                    Log.w("[BUGGY resume job]", "${job.filename}, ${job.idle.toString()}")
                    runOrResumeJob(job.filename, job.idle)
                }
            }
            rLock.unlock()
            sLock.unlock()
            iLock.unlock()
            Log.d("[dispatcher $uid]","插隊任務派送完畢，dispatch 停止")
            return
        }

    }

    suspend fun onWorkerFailed(job: Job){

        eventChannel.send(EventMessage(EventMessageType.WORKER_FAILED, job))
    }

    private suspend fun onWorkerComplete(job: Job){

        this.eventChannel.send(EventMessage(EventMessageType.WORKER_COMPLETE, job))
    }

    suspend fun updatePriorList(filenameList: List<String>){
        pLock.lock()
        this.priorFilenameList.clear()
        this.priorFilenameList.addAll(filenameList)
        pLock.unlock()
        this.eventChannel.send(EventMessage(EventMessageType.JOB_PRIORITY_CHANGE))
    }

    suspend fun commit(filename: String, url: String): Channel<Int> {
        val newJob = Job(filename, url)

        //this.dispatcher()
        // check if file existed, if exists, do not give job to dispatcher
        this.scope.launch {
            withContext(Dispatchers.IO){
                val file = File(context.cacheDir, filename)
                if (file.exists()){
                    newJob.progressChan.send(101)
                    Log.d("[commit]", "$filename 緩存命中")
                    return@withContext
                }else{
                    // todo notify dispatcher the job
                    eventChannel.send(EventMessage(EventMessageType.JOB_COMMITED, newJob))
                }
            }
        }
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
                if (msg) innerChan.send(true)
            }
            innerChan.close()
        }
        withContext(Dispatchers.IO){

            if (!running) {
                Log.d("[Worker]", "${job.filename} 等待開始運行")
                innerChan.receive()
            }
            eventChannel.send(EventMessage(EventMessageType.WORKER_STARTED, job))
            Log.d("[Worker]", "${job.filename} 開始運行辣")

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

            if (!running) {
                eventChannel.send(EventMessage(EventMessageType.WORKER_PAUSED, job))
                Log.d("[Worker]", "${job.filename} 暫停辣")
                innerChan.receive()
                eventChannel.send(EventMessage(EventMessageType.WORKER_RESUMED, job))
                Log.d("[Worker]", "${job.filename} 繼續辣")
            }

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
                    if (!running) {
                        eventChannel.send(EventMessage(EventMessageType.WORKER_PAUSED, job))
                        Log.d("[Worker]", "${job.filename} 暫停辣")
                        innerChan.receive()
                        eventChannel.send(EventMessage(EventMessageType.WORKER_RESUMED, job))
                        Log.d("[Worker]", "${job.filename} 繼續辣")
                    }

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

data class EventMessage(
    val type: EventMessageType,
    val job: Job? = null,
)

enum class EventMessageType(val index: Int){
    WORKER_STARTED(0),
    WORKER_PAUSED(1),
    WORKER_RESUMED(2),
    WORKER_COMPLETE(3),
    WORKER_FAILED(4),
    JOB_COMMITED(5),
    JOB_PRIORITY_CHANGE(6),
    JUST_RERUN(7),


    ;companion object {
        fun fromIndex(ordinal: Int) : EventMessageType {
            return values()[ordinal]
        }
    }
}

// DNT: 1
// Referer: https://bestdori.com/info/cards/1612/Maya-Yamato-So-Music-Is
// sec-ch-ua: "Microsoft Edge";v="105", " Not;A Brand";v="99", "Chromium";v="105"
// sec-ch-ua-mobile: ?0
// sec-ch-ua-platform: "Windows"
// User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.0.0 Safari/537.36 Edg/105.0.1343.42