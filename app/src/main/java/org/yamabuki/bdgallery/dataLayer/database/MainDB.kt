package org.yamabuki.bdgallery.dataLayer.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.yamabuki.bdgallery.dataType.Card
import org.yamabuki.bdgallery.dataType.Manga
import org.yamabuki.bdgallery.dataType.Sticker

@Database(entities = [Card::class, Manga::class, Sticker::class], version = 3)// exportSchema = false)
@TypeConverters(Converters::class)
abstract class MainDB : RoomDatabase() {

    abstract fun cardDao(): CardDao
    abstract fun mangaDao(): MangaDao
    abstract fun stickerDao(): StickerDao

    companion object {
        @Volatile
        private var INSTANCE: MainDB? = null

        fun getDB(context: Context): MainDB {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        MainDB::class.java,
                        "main.db"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }

}