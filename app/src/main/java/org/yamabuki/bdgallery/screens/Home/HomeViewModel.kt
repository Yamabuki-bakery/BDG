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

class HomeViewModel(
    application: Application
) : AndroidViewModel(application) {
    private var _count by mutableStateOf(0)
    private var _state by mutableStateOf(HomeState.UNKNOWN)
    private val myCardRepo: MyCardRepo = MyCardRepo()
    private val cardDao = MainDB.getDB(getApplication()).cardDao()

    val text: String
        get() {
            return "DB Count $_count"
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
            if (result.isSuccess){
                _count =  result.getOrThrow().size
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
        }
    }
    fun countAllCards() {
        viewModelScope.launch {
            cardDao.getCardCount().collect(){
                _count = it
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