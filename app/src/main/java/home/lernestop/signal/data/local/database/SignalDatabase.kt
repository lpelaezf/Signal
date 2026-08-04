package home.lernestop.signal.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import home.lernestop.signal.data.local.converter.Converters
import home.lernestop.signal.data.local.dao.VideoDao
import home.lernestop.signal.data.local.entity.VideoEntity

@Database(entities = [VideoEntity::class], version = 1)
@TypeConverters(Converters::class)
abstract class SignalDatabase: RoomDatabase() {
    abstract fun videoDao(): VideoDao
}