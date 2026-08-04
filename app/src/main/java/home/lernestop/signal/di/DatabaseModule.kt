package home.lernestop.signal.di

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import home.lernestop.signal.data.local.dao.VideoDao
import home.lernestop.signal.data.local.database.SignalDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(app: Application): SignalDatabase {
        return Room.databaseBuilder(
            context = app,
            klass = SignalDatabase::class.java,
            name = "signal_database",
        ).build()
    }

    @Provides
    @Singleton
    fun provideDao(db: SignalDatabase): VideoDao {
        return db.videoDao()
    }
}