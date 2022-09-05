package org.yamabuki.bdgallery.screens.Gallery

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
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

    val cards: List<Card> get() = _cards
    val layout: GalleryLayout get() = _layout

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