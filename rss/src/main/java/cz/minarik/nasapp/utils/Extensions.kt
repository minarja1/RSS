package cz.minarik.nasapp.utils

import android.animation.Animator
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.chimbori.crux.articles.Article
import com.rometools.rome.feed.synd.SyndEntry
import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import cz.minarik.base.common.extensions.dpToPx
import cz.minarik.base.ui.base.BaseFragment
import cz.minarik.nasapp.R
import cz.minarik.nasapp.ui.custom.ArticleDTO
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.jsoup.nodes.Document

fun BaseFragment.shareArticle(article: ArticleDTO) {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, article.link)
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, null)
    startActivity(shareIntent)
}

fun String?.toImageSharedTransitionName(): String {
    return "image|${this}"
}

fun String?.toTitleSharedTransitionName(): String {
    return "title|${this}"
}

fun ImageView.loadImageWithDefaultSettings(
    uri: String?,
    error: Int? = null,
    placeholder: Int? = null,
    fallback: Int? = null,
    crossFade: Boolean = false,
) {
    load(uri) {
        placeholder(placeholder ?: R.drawable.image_placeholder)
        error(error ?: R.drawable.image_placeholder)
        fallback(fallback ?: R.drawable.image_placeholder)
        crossfade(crossFade)
    }
}

fun ImageView.loadImageWithDefaultSettings(
    uri: String?,
    placeholder: Drawable?
) {
    load(uri) {
        placeholder(placeholder)
        error(placeholder)
        fallback(placeholder)
    }
}


fun getSwipeActionItemTouchHelperCallback(
    colorDrawableBackground: ColorDrawable,
    getIcon: ((adapterPosition: Int, viewHolder: RecyclerView.ViewHolder) -> Drawable),
    callback: ((adapterPosition: Int, viewHolder: RecyclerView.ViewHolder) -> Unit),
    iconMarginHorizontal: Int = 16.dpToPx,
    swipeDirs: Int = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
) =
    object : ItemTouchHelper.SimpleCallback(0, swipeDirs) {

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            viewHolder2: RecyclerView.ViewHolder
        ): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDirection: Int) {
            callback.invoke(viewHolder.adapterPosition, viewHolder)
        }

        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            val icon = getIcon.invoke(viewHolder.bindingAdapterPosition, viewHolder)
            val itemView = viewHolder.itemView
            val iconMarginVertical =
                (viewHolder.itemView.height - icon.intrinsicHeight) / 2
            if (dX > 0) {
                colorDrawableBackground.setBounds(
                    itemView.left,
                    itemView.top,
                    dX.toInt(),
                    itemView.bottom
                )
                icon.setBounds(
                    itemView.left + iconMarginHorizontal,
                    itemView.top + iconMarginVertical,
                    itemView.left + iconMarginHorizontal + icon.intrinsicWidth,
                    itemView.bottom - iconMarginVertical
                )
            } else {
                colorDrawableBackground.setBounds(
                    itemView.right + dX.toInt(),
                    itemView.top,
                    itemView.right,
                    itemView.bottom
                )
                icon.setBounds(
                    itemView.right - iconMarginHorizontal - icon.intrinsicWidth,
                    itemView.top + iconMarginVertical,
                    itemView.right - iconMarginHorizontal,
                    itemView.bottom - iconMarginVertical
                )
                icon.level = 0
            }

            colorDrawableBackground.draw(c)

            c.save()

            if (dX > 0)
                c.clipRect(itemView.left, itemView.top, dX.toInt(), itemView.bottom)
            else
                c.clipRect(
                    itemView.right + dX.toInt(),
                    itemView.top,
                    itemView.right,
                    itemView.bottom
                )

            icon.draw(c)

            c.restore()

            super.onChildDraw(
                c,
                recyclerView,
                viewHolder,
                dX,
                dY,
                actionState,
                isCurrentlyActive
            )
        }
    }

fun Document.styleHtml(context: Context) {
    val linkTextColor =
        "#" + Integer.toHexString(
            ContextCompat.getColor(
                context,
                R.color.colorAccent
            ) and 0x00ffffff
        )
    val links = select("a")
    links.attr("style", "color:$linkTextColor;");
}

fun String.styleHtml(context: Context): String {
    val textColor =
        "#" + Integer.toHexString(
            ContextCompat.getColor(
                context,
                R.color.textColorPrimary
            ) and 0x00ffffff
        )
    return "<html><head>" +
            "<style type=\"text/css\">body{color: $textColor; background-color: #000;}" +
            "</style></head>" +
            "<body>$this" +
            "</body></html>"
}

val Article.imgUrlSafe: String?
    get() = (imageUrl?.toString() ?: images?.getOrNull(0)?.srcUrl?.toString())?.replace(
        "http://",
        "https://"
    )

//todo move to base
fun getCircularAnimator(targetView: View, sourceX: Int, sourceY: Int, speed: Long): Animator {
    val finalRadius =
        Math.hypot(targetView.width.toDouble(), targetView.height.toDouble()).toFloat()
    return ViewAnimationUtils.createCircularReveal(targetView, sourceX, sourceY, 0f, finalRadius)
        .apply {
            interpolator = AccelerateDecelerateInterpolator()
            duration = speed
        }
}

fun SyndEntry.guid(): String {
    return "$title$publishedDate"
}


fun OkHttpClient.createCall(url: String): Call = newCall(
    Request.Builder()
        .url(url)
        .header(
            "User-agent",
            "Mozilla/5.0 (compatible) AppleWebKit Chrome Safari"
        ) // some feeds need this to work properly
        .addHeader("accept", "*/*")
        .build()
)

fun Response.toSyncFeed(): SyndFeed? {
    return SyndFeedInput().build(XmlReader(body?.byteStream()))
}