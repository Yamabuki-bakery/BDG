package org.yamabuki.bdgallery.dataLayer.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.yamabuki.bdgallery.dataType.Manga

@Dao
interface MangaDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(manga:List<Manga>)

    @Query("DELETE FROM manga")
    fun clearAll()

    @Query("SELECT COUNT(*) FROM manga")
    fun getMangaCount(): Flow<Int>

}