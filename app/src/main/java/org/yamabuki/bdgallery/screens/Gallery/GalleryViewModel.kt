package org.yamabuki.bdgallery.screens.Gallery

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.yamabuki.bdgallery.R
import org.yamabuki.bdgallery.dataLayer.database.MainDB
import org.yamabuki.bdgallery.dataType.Card


class GalleryViewModel(
    application: Application
) : AndroidViewModel(application) {
    private var _cards  = arrayListOf<Card>().toMutableStateList()
    private var _layout by mutableStateOf(GalleryLayout.LargeImage)
    private var _scroll1stItem by mutableStateOf(0)
    private var _scroll1stItemOffset by mutableStateOf(0)

    var largeImgStateSet = mutableMapOf<Int, LargeImgUIState>()
    val cards: List<Card> get() = _cards
    val layout: GalleryLayout get() = _layout


    // 我諤諤
    val lazylistState = LazyListState()
    val lazyGridState = LazyGridState()


    private val app = application
    private val cardDao = MainDB.getDB(getApplication()).cardDao()
    private val sharedPref = application.getSharedPreferences(application.getString(R.string.app_sharedpref_key), Context.MODE_PRIVATE)
    private val spEditor = sharedPref.edit()


    init {
        getCards()
        _layout = GalleryLayout.fromIndex(sharedPref.getInt(app.getString(R.string.spkey_galleryLayout), 1))
    }

    fun setLayout(){
        _layout = _layout.next()
        viewModelScope.launch {
            spEditor.putInt(app.getString(R.string.spkey_galleryLayout), _layout.index)
            spEditor.commit()
        }
        //Log.d("[setLayout]", "Layout is ${_layout.name}")
    }

    fun getCards(){
        viewModelScope.launch {
            cardDao.getAllCards().collect {
                _cards = it.toMutableStateList()
            }
        }
    }

    fun getLargeCardStateObj(target: Card): LargeImgUIState {
        return LargeImgUIState(target)
    }


}

class LargeImgUIState(card: Card) {
    private var _switchable by mutableStateOf(true)
    private var _trained by mutableStateOf(false)
    private var _progress by mutableStateOf(0)

    val switchable: Boolean get() = _switchable
    val trained: Boolean get() = _trained
    val progress: Int get() = _progress

    var normalBitmap: Bitmap? = null
    var trainedBitmap: Bitmap? = null

    init {
        this._switchable = card.imgNormal && card.imgTrained
        this._trained = (!_switchable) && card.imgTrained

    }
}

enum class GalleryLayout(val index: Int){
    Metadata(0),
    LargeImage(1),
    Grid(2);
    companion object {
        fun fromIndex(ordinal: Int) : GalleryLayout {
            return values()[ordinal]
        }
    }
    fun next() : GalleryLayout{
        return fromIndex((this.index + 1) % 3)
    }
}