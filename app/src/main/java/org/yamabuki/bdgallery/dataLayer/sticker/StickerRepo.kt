package org.yamabuki.bdgallery.dataLayer.sticker

import org.yamabuki.bdgallery.dataLayer.database.StickerDao
import org.yamabuki.bdgallery.dataType.Sticker

interface StickerRepo {

    suspend fun loadAllStickers(stickerDao: StickerDao) : Result<List<Sticker>>

    suspend fun delAllStickers(stickerDao: StickerDao)
}