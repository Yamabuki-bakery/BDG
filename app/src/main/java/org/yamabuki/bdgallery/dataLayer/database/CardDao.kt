package org.yamabuki.bdgallery.dataLayer.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.yamabuki.bdgallery.dataType.Card

@Dao
interface CardDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(cards:List<Card>)

    @Query("DELETE FROM card")
    fun clearAll()

    @Query("SELECT COUNT(*) FROM card")
    fun getCardCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM card WHERE star = :star")
    fun getCardCountByStar(star: Int): Flow<Int>

}