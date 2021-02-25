package cz.minarik.nasapp.utils

import cz.minarik.nasapp.data.datastore.DataStoreManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Code that should be run when starting the app.
 */
object AppStarter {
    fun run(){
        CoroutineScope(Dispatchers.Default).launch {
            //new articles found should always be 0 on app start
            DataStoreManager.setNewArticlesFound(0)
        }
    }
}