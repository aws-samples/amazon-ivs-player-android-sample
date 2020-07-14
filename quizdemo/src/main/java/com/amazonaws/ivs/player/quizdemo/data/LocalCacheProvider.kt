package com.amazonaws.ivs.player.quizdemo.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.amazonaws.ivs.player.quizdemo.data.entity.SourceDataItem

@Database(entities = [SourceDataItem::class], version = 1, exportSchema = false)
abstract class LocalCacheProvider : RoomDatabase() {

    abstract fun sourcesDao(): QuizSourceDao

}
