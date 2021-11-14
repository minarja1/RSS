package cz.minarik.nasapp.utils

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.work.*
import cz.minarik.nasapp.data.db.dao.RSSSourceDao
import cz.minarik.nasapp.data.db.repository.ArticlesRepository
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject

class UpdateArticlesWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams), KoinComponent {

    private val repository: ArticlesRepository by inject()
    private val sourceDao: RSSSourceDao by inject()

    companion object {
        fun run(context: Context): LiveData<WorkInfo> {
            val constraints: Constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            //todo load from settings
//            val work = PeriodicWorkRequestBuilder<UpdateArticlesWorker>(15, TimeUnit.MINUTES)
//                .setConstraints(constraints)
//                .build()

            val work = OneTimeWorkRequestBuilder<UpdateArticlesWorker>()
                .setConstraints(constraints)
                .build()

            val workManager = WorkManager.getInstance(context)

            workManager.enqueue(work)
            return workManager.getWorkInfoByIdLiveData(work.id)
        }
    }

    override suspend fun doWork(): Result = coroutineScope {
        repository.updateArticles(
            sourceUrls = sourceDao.getAllUnblocked().map { it.url },
            coroutineScope = this,
            handleNewArticles = {
                this.launch {
                    NotificationHelper.showNotifications(
                        it,
                        this@UpdateArticlesWorker.applicationContext
                    )
                }
            }
        )
        //we don't care about result
        Result.success()
    }

}