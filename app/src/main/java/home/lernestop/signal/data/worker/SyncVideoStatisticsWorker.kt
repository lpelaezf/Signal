package home.lernestop.signal.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import home.lernestop.signal.core.exception.SignalException
import home.lernestop.signal.data.repo.SignalRepositoryImp

@HiltWorker
class SyncVideoStatisticsWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val repo: SignalRepositoryImp
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            repo.syncVideoStatistics()
            //          future impl: make a notification to notify success
            Result.success()
        } catch (e: SignalException) {
            if (e is SignalException.NetworkException) Result.retry()
            //          future impl: make a notification to notify failure
            else Result.failure()
        }
    }
}
