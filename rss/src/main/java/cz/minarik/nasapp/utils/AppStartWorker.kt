package cz.minarik.nasapp.utils

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.work.*
import cz.minarik.nasapp.data.datastore.DataStoreManager
import cz.minarik.nasapp.data.db.repository.ArticlesRepository
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*

class AppStartWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams), KoinComponent {

    private val repository: ArticlesRepository by inject()

    companion object {
        fun run(context: Context): LiveData<WorkInfo> {
            val constraints: Constraints = Constraints.Builder()
                .build()

            val work = OneTimeWorkRequestBuilder<AppStartWorker>()
                .setConstraints(constraints)
                .build()

            val workManager = WorkManager.getInstance(context)

            workManager.enqueue(work)
            return workManager.getWorkInfoByIdLiveData(work.id)
        }
    }

    override suspend fun doWork(): Result = coroutineScope {
        repository.dbCleanUp(Date().addDays(DataStoreManager.getDbCleanupSettingsItem().first().daysValue))
        //we don't care about result
        Result.success()
    }

}