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
import cz.minarik.nasapp.data.datastore.DataStoreManager
import cz.minarik.nasapp.data.db.entity.ArticleEntity
import cz.minarik.nasapp.data.domain.ArticleDTO
import cz.minarik.nasapp.ui.articles.detail.ArticleDetailActivity
import cz.minarik.nasapp.utils.Constants.Companion.notificationChannelId
import cz.minarik.nasapp.utils.Constants.Companion.notificationDescriptionMaxLength
import kotlinx.coroutines.flow.first

object NotificationHelper {

    suspend fun showNotifications(newArticles: List<ArticleEntity>, context: Context) {
        for (article in newArticles) {
            val intent = Intent(context, ArticleDetailActivity::class.java).apply {
                var flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra(Constants.argArticleDTO, ArticleDTO.fromDb(article))
            }
            val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

            val contentText =
                article.description?.removeHtml()?.ellipsize(notificationDescriptionMaxLength)

            val builder = NotificationCompat.Builder(context, notificationChannelId)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(article.title)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentText(contentText)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(contentText)
                )
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
                onError = { showNotification(context, builder, null) },
                onSuccess = {
                    showNotification(context, builder, it.toBitmap())
                }
            )
            .data(article.image)
            .transformations(CircleCropTransformation())
            .build()

        Coil.enqueue(request)
    }

    private fun showNotification(
        context: Context,
        builder: NotificationCompat.Builder,
        image: Bitmap?
    ) {
        image?.let {
            builder.setLargeIcon(it)
        }
        with(NotificationManagerCompat.from(context)) {
            notify(System.currentTimeMillis().toInt(), builder.build())
        }
    }

}