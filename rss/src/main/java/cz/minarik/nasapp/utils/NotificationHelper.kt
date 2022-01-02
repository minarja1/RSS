package cz.minarik.nasapp.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.toBitmap
import coil.Coil
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import cz.minarik.nasapp.R
import cz.minarik.nasapp.data.db.entity.ArticleEntity
import cz.minarik.nasapp.data.domain.ArticleDTO
import cz.minarik.nasapp.ui.articles.detail.ArticleDetailActivity
import cz.minarik.nasapp.utils.Constants.Companion.notificationChannelId

object NotificationHelper {

    val vibrationPattern = longArrayOf(0, 200, 60, 200)

    fun showNotifications(newArticles: List<ArticleEntity>, context: Context) {
        for (article in newArticles) {
            val intent = Intent(context, ArticleDetailActivity::class.java).apply {
                flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(Constants.argArticleDTO, ArticleDTO.fromDb(article))
            }
            val uniqueInt = (System.currentTimeMillis() and 0xfffffff).toInt()

            val pendingIntent: PendingIntent =
                PendingIntent.getActivity(context, uniqueInt, intent, 0)

            val builder = NotificationCompat.Builder(context, notificationChannelId)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(article.sourceName)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentText(article.title)
                .setSilent(true)
                .setVibrate(vibrationPattern)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            loadImageAndDisplayNotification(article, context, builder)
        }
    }

    private fun loadImageAndDisplayNotification(
        article: ArticleEntity,
        context: Context,
        builder: NotificationCompat.Builder
    ) {
        val request = ImageRequest.Builder(context)
            .target(
                onError = { showNotification(article, context, builder, null) },
                onSuccess = {
                    showNotification(article, context, builder, it.toBitmap())
                }
            )
            .data(article.image)
            .build()

        Coil.enqueue(request)
    }

    private fun showNotification(
        article: ArticleEntity,
        context: Context,
        builder: NotificationCompat.Builder,
        image: Bitmap?
    ) {
        image?.let {
            builder.setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(it)
                    .bigLargeIcon(null)
            )
            builder.setLargeIcon(it)
        } ?: builder.setStyle(
            NotificationCompat.BigTextStyle().bigText(article.title)
        )

        with(NotificationManagerCompat.from(context)) {
            notify(article.hashCode(), builder.build())
        }
    }

}