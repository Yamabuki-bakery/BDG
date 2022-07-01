package org.yamabuki.bdgallery.dataLayer.sticker.impl

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.yamabuki.bdgallery.dataLayer.database.StickerDao
import org.yamabuki.bdgallery.dataLayer.retrofit.Dori
import org.yamabuki.bdgallery.dataLayer.sticker.StickerRepo
import org.yamabuki.bdgallery.dataType.Member
import org.yamabuki.bdgallery.dataType.ServerArea
import org.yamabuki.bdgallery.dataType.Sticker

class MyStickerRepo: StickerRepo {
    override suspend fun loadAllStickers(stickerDao: StickerDao): Result<List<Sticker>> {
        return withContext (Dispatchers.IO) {
            try {
                val defer1 = async (Dispatchers.IO){ Dori.mService._getAllStickers(ServerArea.JP.lower) }
                val defer2 = async (Dispatchers.IO){ Dori.mService._getAllStickers(ServerArea.EN.lower) }
                val defer3 = async (Dispatchers.IO){ Dori.mService._getAllStickers(ServerArea.TW.lower) }
                val defer4 = async (Dispatchers.IO){ Dori.mService._getAllStickers(ServerArea.CN.lower) }
                val defer5 = async (Dispatchers.IO){ Dori.mService._getAllStickers(ServerArea.KR.lower) }

                val jsonStrings = awaitAll(defer1, defer2, defer3, defer4, defer5)
                val stickerList = jsonParse(jsonStrings)
                stickerDao.insertAll(stickerList)
                Result.success(stickerList)
            } catch (err: Exception) {
                Log.e("[loadAllStickers]", err.stackTraceToString())
                Result.failure(IllegalArgumentException("[loadAllStickers] 過於惡俗！"))
            }
        }
    }

    override suspend fun delAllStickers(stickerDao: StickerDao) {
        return withContext(Dispatchers.IO) {
            stickerDao.clearAll()
        }
    }
}

private fun jsonParse (jsonStrings: List<String>): List<Sticker> {
    val result = mutableListOf<Sticker>()
    // 屌查每一個服務器區域的結果
    for (i in jsonStrings.indices) {
        val j1 = jsonStrings[i]
        val jArr = JSONArray(j1)
        val thisArea = ServerArea.fromIndex(i)
        // 屌查每一個文件名
        for (j in 0 until jArr.length()) {
            val fileName = jArr.getString(j)
            if (!fileNameValid(fileName)) continue  // 無效，跳過
            // 檢查 result 是否已有，如有，則增加 server area，如無，則新創一個 sticker

            val exist = result.firstOrNull { it.fileName == fileName }
            if (exist != null) {
                // 這玩意是副本還是指針？？
                exist.areaAvailability.add(thisArea)
                continue
            } else {
                val member = findMember(fileName)
                val area = mutableListOf(thisArea)
                val newSticker = Sticker(fileName, member, area)
                result.add(newSticker)
            }
        }
    }
    return result
}

private fun fileNameValid(fileName: String): Boolean{
    return try {
        if (!fileName.startsWith("stamp_"))
            throw IllegalArgumentException("$fileName 前綴過於惡俗！")
        if (!fileName.endsWith(".png"))
            throw IllegalArgumentException("$fileName 後綴過於惡俗！")
        if ("icon" in fileName)
            throw IllegalArgumentException("$fileName icon 過於惡俗！")
        true
    } catch (e: IllegalArgumentException){
        Log.d("[StickerFileNameValidation]", "${e.message}")
        false
    }
}

private fun findMember(fileName: String): Member?{
    try {
        val regex = Regex("""stamp_(\d{3})(\S{3})\.png""")
        val match = regex.find(fileName)
        if (match == null) return null
        val memId = match.groups[1]!!.value.toInt()
        return Member.fromId(memId)
    }catch (e: Exception){
        Log.d("[findMember]", "${e.message}")
        return null
    }
}