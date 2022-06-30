package org.yamabuki.bdgallery.dataLayer.manga

import org.yamabuki.bdgallery.dataLayer.database.MangaDao
import org.yamabuki.bdgallery.dataType.Manga

interface MangaRepo {

    suspend fun loadAllManga(mangaDao: MangaDao) : Result<List<Manga>>

    suspend fun delAllManga(mangaDao: MangaDao)
}