package org.yamabuki.bdgallery.screens.Home

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.yamabuki.bdgallery.dataLayer.card.impl.MyCardRepo
import org.yamabuki.bdgallery.dataLayer.database.MainDB
import org.yamabuki.bdgallery.dataLayer.manga.impl.MyMangaRepo

class HomeViewModel(
    application: Application
) : AndroidViewModel(application) {
    private var _cardCount by mutableStateOf(-1)
    private var _mangaCount by mutableStateOf(-1)
    private var _state by mutableStateOf(HomeState.UNKNOWN)
    private val myCardRepo: MyCardRepo = MyCardRepo()
    private val myMangaRepo = MyMangaRepo()
    private val cardDao = MainDB.getDB(getApplication()).cardDao()
    private val mangaDao = MainDB.getDB(getApplication()).mangaDao()

    val cardCount: Int
        get() {
            return _cardCount
        }
    val mangaCount: Int
        get() {
            return _mangaCount
        }

    val state: HomeState
        get() {
            return _state
        }

    fun refresh(){
        _state = HomeState.LOADING
        viewModelScope.launch {
            //val cardDao = MainDB.getDB(getApplication()).cardDao()
            val result = myCardRepo.loadAllCards(cardDao)
            val result2 = myMangaRepo.loadAllManga(mangaDao)
            if (result.isSuccess and result2.isSuccess){
                _cardCount = result.getOrThrow().size
                _mangaCount = result2.getOrThrow().size
                _state = HomeState.SUCCESS
            }else{
                _state = HomeState.FAILED
            }
        }
    }
    fun clear(){
        viewModelScope.launch {
            //val cardDao = MainDB.getDB(getApplication()).cardDao()
            myCardRepo.delAllCards(cardDao)
            myMangaRepo.delAllManga(mangaDao)
        }
    }
    fun countAllCards() {
        viewModelScope.launch {
            cardDao.getCardCount().collect(){
                _cardCount = it
            }
        }
    }
    fun countAllManga() {
        viewModelScope.launch {
            mangaDao.getMangaCount().collect(){
                _mangaCount = it
            }
        }
    }
}

enum class HomeState(){
    LOADING,
    SUCCESS,
    FAILED,
    UNKNOWN,
}