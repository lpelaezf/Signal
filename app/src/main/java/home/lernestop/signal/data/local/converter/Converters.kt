package home.lernestop.signal.data.local.converter

import androidx.room.TypeConverter
import kotlin.time.Instant

class Converters {

    @TypeConverter
    fun instantToTimeStamp(instant: Instant): Long = instant.toEpochMilliseconds()

    @TypeConverter
    fun timeStampToInstant(timestamp: Long): Instant = Instant.fromEpochMilliseconds(timestamp)
}