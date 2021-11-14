package cz.minarik.nasapp.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import cz.minarik.nasapp.R
import cz.minarik.nasapp.data.db.entity.ArticleEntity
import cz.minarik.nasapp.ui.SplashActivity
import cz.minarik.nasapp.utils.Constants.Companion.notificationChannelId
import cz.minarik.nasapp.utils.Constants.Companion.notificationId

object NotificationHelper {

    fun showNotification(newArticles: List<ArticleEntity>, context: Context) {
        //todo handle cyka blyat

        // Create an explicit intent for an Activity in your app
        val intent = Intent(context, SplashActivity::class.java).apply {
            var flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

        val builder = NotificationCompat.Builder(context, notificationChannelId)
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle("My notification")
            .setContentText("Much longer text that cannot fit one line...")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Much longer text that cannot fit one line...")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
    }

}