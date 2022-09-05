package org.yamabuki.bdgallery.screens.Home

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.yamabuki.bdgallery.R
import org.yamabuki.bdgallery.dataLayer.card.impl.MyCardRepo
import org.yamabuki.bdgallery.dataLayer.database.MainDB
import org.yamabuki.bdgallery.dataLayer.manga.impl.MyMangaRepo
import org.yamabuki.bdgallery.dataLayer.sticker.impl.MyStickerRepo
import java.time.LocalDateTime
import java.time.ZoneOffset

//import java.util.*

class HomeViewModel(
    application: Application
) : AndroidViewModel(application) {
    // UI State
    private var _cardCount by mutableStateOf(-1)
    private var _mangaCount by mutableStateOf(-1)
    private var _stickerCount by mutableStateOf(-1)
    private var _state by mutableStateOf(HomeState.UNKNOWN)
    private var _appUpdate by mutableStateOf(false)
    private var _lastUpdateTime by mutableStateOf(LocalDateTime.now())
    private val _cardCountByStar = arrayListOf(-1, -1, -1, -1).toMutableStateList()

    val cardCount: Int get() = _cardCount
    val mangaCount: Int get() = _mangaCount
    val stickerCount: Int get() = _stickerCount
    val state: HomeState get() = _state
    val appUpdate: Boolean get() = _appUpdate
    val lastUpdateTime: LocalDateTime get() = _lastUpdateTime
    val cardCountByStar: List<Int> get() = _cardCountByStar

    private val app = application
    private val myCardRepo: MyCardRepo = MyCardRepo()
    private val myMangaRepo = MyMangaRepo()
    private val myStickerRepo = MyStickerRepo()
    private val cardDao = MainDB.getDB(getApplication()).cardDao()
    private val mangaDao = MainDB.getDB(getApplication()).mangaDao()
    private val stickerDao = MainDB.getDB(getApplication()).stickerDao()
    private val sharedPref = application.getSharedPreferences(application.getString(R.string.app_sharedpref_key), Context.MODE_PRIVATE)
    private val spEditor = sharedPref.edit()

    init {
        countAllCards()
        countAllManga()
        countAllStickers()
       // viewModelScope.launch {
            _lastUpdateTime = LocalDateTime.ofEpochSecond(sharedPref.getLong(app.getString(R.string.spkey_lastrefreshtime), 0), 0, ZoneOffset.UTC)
      //  }
    }


    fun refresh() {
        _state = HomeState.LOADING
        viewModelScope.launch {
            //val cardDao = MainDB.getDB(getApplication()).cardDao()
            val result = myCardRepo.loadAllCards(cardDao)
            val result2 = myMangaRepo.loadAllManga(mangaDao)
            val result3 = myStickerRepo.loadAllStickers(stickerDao)
            if (result.isSuccess and result2.isSuccess and result3.isSuccess) {
                _cardCount = result.getOrThrow().size
                _mangaCount = result2.getOrThrow().size
                _stickerCount = result3.getOrThrow().size
                _state = HomeState.SUCCESS
                spEditor.putLong(app.getString(R.string.spkey_lastrefreshtime),
                    LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
                spEditor.commit()
                _lastUpdateTime = LocalDateTime.now()
            } else {
                _state = HomeState.FAILED
            }
        }
    }

    fun clear() {
        viewModelScope.launch {
            //val cardDao = MainDB.getDB(getApplication()).cardDao()
            myCardRepo.delAllCards(cardDao)
            myMangaRepo.delAllManga(mangaDao)
            myStickerRepo.delAllStickers(stickerDao)
        }
    }

    fun countAllCards() {
        for (i in 0 until 4) {
            viewModelScope.launch {
                cardDao.getCardCountByStar(i + 1).collect {
                    _cardCountByStar[i] = it
                }
            }
        }
        viewModelScope.launch {
            cardDao.getCardCount().collect {
                _cardCount = it
            }
        }
    }

    fun countAllManga() {
        viewModelScope.launch {
            mangaDao.getMangaCount().collect() {
                _mangaCount = it
            }
        }
    }

    fun countAllStickers() {
        viewModelScope.launch {
            stickerDao.getStickerCount().collect() {
                _stickerCount = it
            }
        }
    }
}


enum class HomeState() {
    LOADING,
    SUCCESS,
    FAILED,
    UNKNOWN,
}