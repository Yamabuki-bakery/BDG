package org.yamabuki.bdgallery.dataLayer.card.impl

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.yamabuki.bdgallery.dataLayer.card.CardRepo
import org.yamabuki.bdgallery.dataLayer.database.CardDao
import org.yamabuki.bdgallery.dataLayer.retrofit.Dori
import org.yamabuki.bdgallery.dataType.Card
import org.yamabuki.bdgallery.dataType.CardArea
import org.yamabuki.bdgallery.dataType.CardAttr
import org.yamabuki.bdgallery.dataType.Member

class MyCardRepo : CardRepo {
    override suspend fun loadAllCards(cardDao: CardDao): Result<List<Card>> {
        //val card = Card(1, "test", CardAttr.PURE, Member.SAYA,4, Date(0))
        return withContext(Dispatchers.IO) {
            try {
                val jsonStr = Dori.retrofitService.getCards()
                val cardList = cardJsonParse(jsonStr)
                cardDao.insertAll(cardList)
                Result.success(cardList)
            } catch (err: Exception) {
                Log.e("[loadAllCards]", err.stackTraceToString())
                Result.failure(IllegalArgumentException("[loadAllCards] 過於惡俗！"))
            }
        }
    }
    override suspend fun delAllCards(cardDao: CardDao){
        return withContext(Dispatchers.IO) {
            cardDao.clearAll()
        }
    }
}

private fun cardJsonParse(jsonStr: String): List<Card> {
    val j1 = JSONObject(jsonStr)
    val ids: Iterator<String> = j1.keys()
    val cardArray: MutableList<Card> = ArrayList()

    ids.forEach { id: String ->
        val j2 = j1.getJSONObject(id)
        val attr: CardAttr = when (j2.getString("attribute")) {
            "pure" -> CardAttr.PURE
            "powerful" -> CardAttr.POWERFUL
            "happy" -> CardAttr.HAPPY
            "cool" -> CardAttr.COOL
            else -> {
                throw IllegalArgumentException("[cardJsonParse] Card attr 過於惡俗！")
            }
        }
        val member: Member =
            Member.fromIndex(j2.getInt("characterId") - 1) // member enum start by 0
        val (areaCode, title) = getFirstNonNullStr(j2.getJSONArray("prefix"))
        val star: Int = j2.getInt("rarity")
        val (_, releasedAtStr) = getFirstNonNullStr(j2.getJSONArray("releasedAt"))
        val releaseAt = releasedAtStr.toLong()
        val type: String = j2.getString("type")
        val resSet = j2.getString("resourceSetName")
        var imgNormal: Boolean = false;
        var imgTrained: Boolean = false
        if (type == "birthday" || type == "kirafes") {
            // only trained
            imgTrained = true

        } else {
            if (star >= 3) {
                imgTrained = true
            }
            imgNormal = true
        }
        val newCard = Card(
            id.toInt(), title, attr, member, star, releaseAt,
            resSet, imgNormal, imgTrained, CardArea.fromIndex(areaCode)
        )
        cardArray.add(newCard)
    }
    Log.d("[cardJsonParse]", "card array length ${cardArray.size}")
    return cardArray
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