package org.yamabuki.bdgallery.dataLayer.manga.impl

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.yamabuki.bdgallery.dataLayer.database.MangaDao
import org.yamabuki.bdgallery.dataLayer.manga.MangaRepo
import org.yamabuki.bdgallery.dataLayer.retrofit.Dori
import org.yamabuki.bdgallery.dataType.Manga
import org.yamabuki.bdgallery.dataType.Member
import org.yamabuki.bdgallery.dataType.ServerArea

class MyMangaRepo : MangaRepo{
    override suspend fun loadAllManga(mangaDao: MangaDao): Result<List<Manga>> {
        return withContext(Dispatchers.IO) {
            try {
                val jsonStr = Dori.retrofitService.getAllManga()
                val mangaList = jsonParse(jsonStr)
                mangaDao.insertAll(mangaList)
                Result.success(mangaList)
            } catch (err: Exception) {
                Log.e("[loadAllManga]", err.stackTraceToString())
                Result.failure(IllegalArgumentException("[loadAllManga] 過於惡俗！"))
            }
        }
    }

    override suspend fun delAllManga(mangaDao: MangaDao){
        return withContext(Dispatchers.IO) {
            mangaDao.clearAll()
        }
    }
}

private fun jsonParse(jsonStr: String): List<Manga> {
    val j1 = JSONObject(jsonStr)
    val ids: Iterator<String> = j1.keys()
    val mangaArray: MutableList<Manga> = ArrayList()

    ids.forEach { id: String ->
        val j2 = j1.getJSONObject(id)
        val assetName = j2.getString("assetBundleName")

        val titles = j2.getJSONArray("title")
        val availableArea = mutableListOf<ServerArea>()
        val t2 = mutableListOf<String?>()
        for (i in 0 until titles.length()) {
            val title = strOrNull(titles.getString(i))
            t2.add(title)
            if (title != null) availableArea.add(ServerArea.fromIndex(i))
        }

        val (_, publicStartAtStr) = getFirstNonNullStr(j2.getJSONArray("publicStartAt"))
        val publicStartAt = publicStartAtStr.toLong()

        val charIds = j2.getJSONArray("characterId")
        val characters = mutableListOf<Member>()
        //Log.d("[jsonParse]", "Processing manga id $id")
        for (i in 0 until charIds.length()){
            try{
                val member = Member.fromId(charIds.getInt(i))
                characters.add(member)
            }catch (e: ArrayIndexOutOfBoundsException) {
                continue
            }
        }

        val newEpisode = Manga(
            id.toInt(), assetName, t2[0], t2[1], t2[2], t2[3], t2[4],
            publicStartAt, availableArea, ("fourframe" in assetName), characters
        )
        mangaArray.add(newEpisode)
    }
    Log.d("[mangaJsonParse]", "Manga array length ${mangaArray.size}")
    return mangaArray
}

private fun strOrNull(string: String): String?{
    return if(string == "null") null else string
}

private fun getFirstNonNullStr(arr: JSONArray): Triple<Int, String, Unit> {
    for (i in 0 until arr.length()) {
        try {
            val str = arr.getString(i)
            if (str == "null") {
                //Log.d("[getFirstNonNullStr]", "The str is $str !")
                throw JSONException("The str is $str !")
            }
            return Triple(i, str, Unit)
        } catch (e: JSONException) {
            continue
        }
    }
    throw IllegalArgumentException("[getFirstNonNullStr] jsonarray 過於惡俗！沒有不是 null 的")
}