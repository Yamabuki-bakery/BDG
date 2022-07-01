package org.yamabuki.bdgallery.dataLayer.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.yamabuki.bdgallery.dataType.Sticker

@Dao
interface StickerDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(stickers: List<Sticker>)

    @Query("DELETE FROM sticker")
    fun clearAll()

    @Query("SELECT COUNT(*) FROM sticker")
    fun getStickerCount(): Flow<Int>

}