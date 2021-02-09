package cz.minarik.nasapp.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.InsetDrawable
import android.net.Uri
import android.os.Build
import android.text.format.DateUtils
import android.util.TypedValue
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.annotation.MenuRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.PopupMenu
import androidx.browser.customtabs.CustomTabsCallback
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.chimbori.crux.articles.Article
import cz.minarik.base.common.extensions.LastDividerItemDecorator
import cz.minarik.base.common.extensions.dpToPx
import cz.minarik.base.common.extensions.pxToDp
import cz.minarik.base.common.extensions.toast
import cz.minarik.base.ui.base.BaseFragment
import cz.minarik.nasapp.R
import cz.minarik.nasapp.ui.custom.ArticleDTO
import cz.minarik.nasapp.utils.Constants.Companion.RECYCLER_MAX_VERTICAL_OFFEST_FOR_SMOOTH_SCROLLING
import me.saket.bettermovementmethod.BetterLinkMovementMethod
import org.jsoup.nodes.Document
import org.koin.android.ext.android.getKoin
import org.koin.androidx.viewmodel.ViewModelParameter
import org.koin.androidx.viewmodel.koin.getViewModel
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier
import timber.log.Timber
import java.net.URL
import java.util.*

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


//todo move to Base________________________________________________________________________________
fun Context.copyToClipBoard(label: String, text: String) {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
}

@SuppressLint("RestrictedApi")
fun PopupMenu.iconizeMenu(resources: Resources, iconPadding: Int = 4.dpToPx) {
    if (menu is MenuBuilder) {
        val menuBuilder = menu as MenuBuilder
        menuBuilder.setOptionalIconsVisible(true)
        for (item in menuBuilder.visibleItems) {
            val iconMarginPx =
                TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    iconPadding.toFloat(),
                    resources.displayMetrics
                ).toInt()
            if (item.icon != null) {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                    item.icon =
                        InsetDrawable(item.icon, iconMarginPx, 0, iconMarginPx, 0)
                } else {
                    item.icon =
                        object :
                            InsetDrawable(item.icon, iconMarginPx, 0, iconMarginPx, 0) {
                            override fun getIntrinsicWidth(): Int {
                                return intrinsicHeight + iconMarginPx + iconMarginPx
                            }
                        }
                }
            }
        }
    }
}

fun RecyclerView.dividerFullWidth() {
    val listDivider = LastDividerItemDecorator(
        AppCompatResources.getDrawable(
            context,
            R.drawable.recyclerview_divider_full_horizontal
        )!!
    )
    addItemDecoration(listDivider)
}


/**
 * Custom Tabs warmUp routine with optional Uri preloading.
 */
fun Context.warmUpBrowser(uriToPreload: Uri? = null) {
    val customTabsConnection = object : CustomTabsServiceConnection() {
        override fun onCustomTabsServiceConnected(name: ComponentName, client: CustomTabsClient) {
            client.run {
                warmup(0)
                uriToPreload?.let {
                    val customTabsSession = newSession(object : CustomTabsCallback() {})
                    val success = customTabsSession?.mayLaunchUrl(it, null, null)
                    Timber.i("Preloading url $it ${if (success == true) "SUCCESSFUL" else "FAILED"}")
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
        }
    }
    //init Custom tabs services
    val success = CustomTabsClient.bindCustomTabsService(
        this,
        CHROME_PACKAGE,
        customTabsConnection
    )
    Timber.i("Binding Custom Tabs service ${if (success) "SUCCESSFUL" else "FAILED"}")
}


fun <T> compareLists(first: List<T>, second: List<T>): Boolean {

    if (first.size != second.size) {
        return false
    }

    return first.zip(second).all { (x, y) ->
        x == y
    }
}

fun Intent.addAppReferrer(context: Context) {
    val scheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
        Intent.URI_ANDROID_APP_SCHEME
    } else {
        1 shl 1
    }
    putExtra(
        Intent.EXTRA_REFERRER,
        Uri.parse("${scheme}//${context.packageName}")
    )
}

const val CHROME_PACKAGE = "com.android.chrome"

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

        customTabsBuilder.setStartAnimations(this, R.anim.slide_in_right, R.anim.slide_out_left)
        customTabsBuilder.setExitAnimations(
            this,
            android.R.anim.slide_in_left,
            android.R.anim.slide_out_right
        )

        val intent = customTabsBuilder.build()

        intent.intent.addAppReferrer(this)

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

@Suppress("unused")
fun Fragment.setTransparentStatusBar(transparent: Boolean = true) {
    (activity as AppCompatActivity).setTransparentStatusBar(transparent)
}


fun Activity.setTransparentStatusBar(transparent: Boolean = true) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        window.apply {
            if (transparent) {
                addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                statusBarColor = Color.TRANSPARENT
            } else {
                clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                statusBarColor = Color.BLACK
            }
        }
    }
}

inline fun <reified VM : ViewModel> Fragment.sharedGraphViewModel(
    @IdRes navGraphId: Int,
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null
) = lazy {
    val store = findNavController().getViewModelStoreOwner(navGraphId).viewModelStore
    getKoin().getViewModel(ViewModelParameter(VM::class, qualifier, parameters, null, store, null))
}

fun Activity.hideKeyboard() {
    findViewById<View>(android.R.id.content).hideKeyboard()
}

fun Fragment.hideKeyboard() {
    (activity as AppCompatActivity).findViewById<View>(android.R.id.content)?.hideKeyboard()
}

fun Activity.openKeyboard() {
    findViewById<View>(android.R.id.content).openKeyboard()
}

fun Fragment.openKeyboard() {
    (activity as AppCompatActivity).findViewById<View>(android.R.id.content)?.openKeyboard()
}


fun hideKeyboard(context: Context, view: View) {
    val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.getWindowToken(), 0)
}

fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

fun View.openKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
}