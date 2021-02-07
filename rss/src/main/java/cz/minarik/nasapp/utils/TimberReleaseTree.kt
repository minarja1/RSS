package cz.minarik.nasapp.utils

import android.util.Log
import timber.log.Timber


//todo crashlytics
class TimberReleaseTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, throwable: Throwable?) {

//        val crashlytics = FirebaseCrashlytics.getInstance()
        if (priority == Log.ERROR) {
//            if (LoginManager.isUserLoggedIn()) {
//                LoginManager.getCurrentlyLoggedUserEmail()
//                    ?.let { crashlytics.setUserId(it) }
//            }
//
//            crashlytics.setCustomKey(CRASHLYTICS_KEY_PRIORITY, priority);
//            tag?.let {
//                crashlytics.setCustomKey(CRASHLYTICS_KEY_TAG, tag);
//            }
//            crashlytics.setCustomKey(CRASHLYTICS_KEY_MESSAGE, message);
//
//            crashlytics.recordException(throwable ?: Exception(message).apply {
//                stackTrace = arrayOf(StackTraceElement(message, message, "", 69))
//            })

        } else return
    }

    companion object {
        private const val CRASHLYTICS_KEY_PRIORITY = "priority"
        private const val CRASHLYTICS_KEY_TAG = "tag"
        private const val CRASHLYTICS_KEY_MESSAGE = "message"
    }

}