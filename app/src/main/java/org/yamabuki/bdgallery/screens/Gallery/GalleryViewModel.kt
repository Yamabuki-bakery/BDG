package org.yamabuki.bdgallery.screens.Gallery

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import org.yamabuki.bdgallery.R
import org.yamabuki.bdgallery.dataLayer.ImageLoader.ImageManager
import org.yamabuki.bdgallery.dataLayer.database.MainDB
import org.yamabuki.bdgallery.dataType.Card
import java.io.File


class GalleryViewModel(
    application: Application
) : AndroidViewModel(application) {
    private var _cards  = arrayListOf<Card>().toMutableStateList()  // 所有卡片列表
    private var _layout by mutableStateOf(GalleryLayout.LargeImage)  // UI 顯示佈局狀態

    var largeImgStateSet = mutableMapOf<Int, LargeImgUIState>()  // 大卡片的 UI 狀態數據
    val cards: List<Card> get() = _cards
    val layout: GalleryLayout get() = _layout


    val lazyGridState = LazyGridState()

    private val app = application
    private val cardDao = MainDB.getDB(getApplication()).cardDao()
    private val sharedPref = application.getSharedPreferences(application.getString(R.string.app_sharedpref_key), Context.MODE_PRIVATE)
    private val spEditor = sharedPref.edit()
    private val imageManager = ImageManager(app)


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
        val cardId = target.id
        if (cardId !in largeImgStateSet){
            largeImgStateSet.put(cardId, LargeImgUIState(target, coroutineScope = viewModelScope, imageManager = imageManager))
        }
        return largeImgStateSet[cardId]!!
    }
}

class LargeImgUIState(val card: Card, val coroutineScope: CoroutineScope, val imageManager: ImageManager) {
    private var _switchable by mutableStateOf(true)
    private var _trainable by mutableStateOf(false)
    private var _progress by mutableStateOf(-1)
    private var _trained by mutableStateOf(false)

    val switchable: Boolean get() = _switchable
    val trainablle: Boolean get() = _trainable
    val progress: Int get() = _progress
    val trained: Boolean get() = _trained

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        this._switchable = card.imgNormal && card.imgTrained
        this._trainable = card.imgTrained
        this._trained = (!this._switchable) && this.trainablle
        coroutineScope.launch {
            val trained = _trained
            withContext(Dispatchers.IO){
                imageManager.checkImage(card.getCGFilename(trained), card.getCGurl(trained)).collect {
                    withContext(Dispatchers.Main){
                        _progress = (it * 100).toInt()
                        //Log.d("LargeImgUIState", "The progress is $_progress")
                    }
                }
            }
        }
    }
    fun getFile(): File{
        val filename = this.card.getCGFilename(_trained)
        return File(imageManager.getCacheDir(), filename)
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