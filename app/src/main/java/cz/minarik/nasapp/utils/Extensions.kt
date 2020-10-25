package cz.minarik.nasapp.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.text.format.DateUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.net.toUri
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import cz.minarik.base.common.extensions.toast
import cz.minarik.nasapp.R
import cz.minarik.nasapp.utils.Constants.Companion.RECYCLER_MAX_VERTICAL_OFFEST_FOR_SMOOTH_SCROLLING
import me.saket.bettermovementmethod.BetterLinkMovementMethod
import timber.log.Timber
import java.net.URL
import java.util.*

fun ImageView.loadImageWithDefaultSettings(
    uri: String?,
    error: Int? = null,
    placeholder: Int? = null,
    crossFade: Boolean = false,
) {
    load(uri) {
        placeholder(placeholder ?: R.drawable.image_placeholder)
        error(error ?: R.drawable.image_placeholder)
        crossfade(crossFade)
    }
}

private const val CHROME_PACKAGE = "com.android.chrome"

fun Context.openCustomTabs(
    uri: Uri,
    customTabsBuilder: CustomTabsIntent.Builder = CustomTabsIntent.Builder()
) {
    try {
        customTabsBuilder.setToolbarColor(
            ContextCompat.getColor(
                this,
                cz.minarik.base.R.color.colorToolbar
            )
        )
        customTabsBuilder.addDefaultShareMenuItem()
        val intent = customTabsBuilder.build()

        //to prevent "choose app" dialog -> open in CustomTabs if possible
        getCustomTabsPackages(this, uri)?.let {
            for (resolveInfo in it) {
                if (resolveInfo.activityInfo.packageName == CHROME_PACKAGE) {
                    intent.intent.setPackage(CHROME_PACKAGE)
                    break
                }
            }
        }
        intent.launchUrl(this, uri)
    } catch (e: ActivityNotFoundException) {
        Timber.e(e)
        toast(R.string.common_base_error)
    }
}

fun getCustomTabsPackages(context: Context, uri: Uri): ArrayList<ResolveInfo>? {
    val pm: PackageManager = context.packageManager
    // Get default VIEW intent handler.
    val activityIntent =
        Intent(Intent.ACTION_VIEW, uri)

    // Get all apps that can handle VIEW intents.
    val resolvedActivityList: List<ResolveInfo> = pm.queryIntentActivities(activityIntent, 0)
    val packagesSupportingCustomTabs: ArrayList<ResolveInfo> = ArrayList()
    for (info in resolvedActivityList) {
        val serviceIntent = Intent()
        serviceIntent.action = ACTION_CUSTOM_TABS_CONNECTION
        serviceIntent.setPackage(info.activityInfo.packageName)
        if (pm.resolveService(serviceIntent, 0) != null) {
            packagesSupportingCustomTabs.add(info)
        }
    }
    return packagesSupportingCustomTabs
}


//todo do base
fun Drawable.tint(context: Context, color: Int): Drawable {
    val wrapDrawable: Drawable? = DrawableCompat.wrap(this)
    return wrapDrawable?.let {
        DrawableCompat.setTint(
            it,
            ContextCompat.getColor(context, color)
        )
        it
    } ?: this
}

fun URL.getFavIcon(): String {
    return "https://www.google.com/s2/favicons?sz=64&domain_url=$host"
}

val Int.pxToDp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()

val Int.dpToPx: Float
    get() = (this * Resources.getSystem().displayMetrics.density)


fun TextView.handleHTML(context: Context) {
    movementMethod = BetterLinkMovementMethod.newInstance().apply {
        setOnLinkClickListener { textView, url ->
            context.openCustomTabs(url.toUri())
            true
        }
        setOnLinkLongClickListener { textView, url ->
            // Handle long-click or return false to let the framework handle this link.
            false
        }
        setOnClickListener {

        }
    }
}

fun String.getHostFromUrl(): String? {
    return try {
        val url = URL(this)
        url.host.replace("www.", "")
    } catch (e: Exception) {
        Timber.e(e)
        null
    }
}

fun RecyclerView.scrollToTop(smooth: Boolean = false) {
    val smoothScroll =
        smooth && computeVerticalScrollOffset() > RECYCLER_MAX_VERTICAL_OFFEST_FOR_SMOOTH_SCROLLING
    if (smoothScroll) {
        smoothScrollToPosition(0);
    } else {
        scrollToPosition(0)
    }
}


val Context.isInternetAvailable: Boolean
    get() {
        var result = false
        val connectivityManager =
            this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val actNw =
                connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
            result = when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.run {
                connectivityManager.activeNetworkInfo?.run {
                    result = when (type) {
                        ConnectivityManager.TYPE_WIFI -> true
                        ConnectivityManager.TYPE_MOBILE -> true
                        ConnectivityManager.TYPE_ETHERNET -> true
                        else -> false
                    }

                }
            }
        }
        return result
    }

fun getSwipeActionItemTouchHelperCallback(
    colorDrawableBackground: ColorDrawable,
    getIcon: ((adapterPosition: Int, viewHolder: RecyclerView.ViewHolder) -> Drawable),
    callback: ((adapterPosition: Int, viewHolder: RecyclerView.ViewHolder) -> Unit),
    iconMarginHorizontal: Int = 16.dpToPx.toInt()
) =
    object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

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
            val icon = getIcon.invoke(viewHolder.adapterPosition, viewHolder)
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

fun Date.toTimeElapsed(pastOnly: Boolean = true): CharSequence {
    var actualTime = time
    if (pastOnly && System.currentTimeMillis() - time < 0) actualTime =
        System.currentTimeMillis() //API sometimes returns pubDates in future :(
    return DateUtils.getRelativeTimeSpanString(
        actualTime,
        System.currentTimeMillis(),
        DateUtils.SECOND_IN_MILLIS,
        DateUtils.FORMAT_ABBREV_ALL
    )
}

val screenWidth: Int get() = Resources.getSystem().displayMetrics.widthPixels
val screenWidthDp: Int get() = screenWidth.pxToDp
val screenHeight: Int get() = Resources.getSystem().displayMetrics.heightPixels
val screenHeightDp: Int get() = screenHeight.pxToDp


fun RecyclerView.isScrolledToTop(): Boolean {
    return !canScrollVertically(-1)
}
