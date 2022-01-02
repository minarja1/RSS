package cz.minarik.nasapp.utils

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.work.*
import cz.minarik.nasapp.data.db.dao.RSSSourceDao
import cz.minarik.nasapp.data.db.repository.ArticlesRepository
import kotlinx.coroutines.coroutineScope
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber
import java.util.concurrent.TimeUnit

class UpdateArticlesWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams), KoinComponent {

    private val repository: ArticlesRepository by inject()
    private val sourceDao: RSSSourceDao by inject()

    companion object {
        private const val workName = "updateArticles"
        private const val periodicWorkName = "periodicWorkName"

        fun run(context: Context): LiveData<WorkInfo> {
            val constraints: Constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            //todo load from settings

            val workManager = WorkManager.getInstance(context)
            val work: WorkRequest

            if (ArticlesRepository.fakeNewArticlesForNotification) {
                work = OneTimeWorkRequestBuilder<UpdateArticlesWorker>()
                    .setConstraints(constraints)
                    .build()
                workManager.enqueueUniqueWork(
                    workName,
                    ExistingWorkPolicy.KEEP,
                    work
                )
            } else {
                work = PeriodicWorkRequestBuilder<UpdateArticlesWorker>(15, TimeUnit.MINUTES)
                    .setConstraints(constraints)
                    .build()
                workManager.enqueueUniquePeriodicWork(
                    periodicWorkName,
                    ExistingPeriodicWorkPolicy.KEEP,
                    work
                )
            }

            return workManager.getWorkInfoByIdLiveData(work.id)
        }
    }

    override suspend fun doWork(): Result {
        Timber.i("Working")
        repository.updateArticles(
            sourceUrls = sourceDao.getAllUnblocked().map { it.url },
            coroutineScope = coroutineScope { this },
            handleNewArticles = {
                NotificationHelper.showNotifications(
                    it,
                    this@UpdateArticlesWorker.applicationContext
                )
            }
        )
        //we don't care about result
        return Result.success()
    }

}