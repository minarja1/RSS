package cz.minarik.nasapp.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.widget.ImageView
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION
import androidx.core.content.ContextCompat
import coil.api.load
import cz.minarik.base.common.extensions.toast
import cz.minarik.nasapp.R
import java.util.*

fun ImageView.loadImageWithDefaultSettings(
    uri: String?,
    error: Int? = null,
    placeholder: Int? = null
) {
    load(uri) {
        placeholder(placeholder ?: R.drawable.image_placeholder)
        error(error ?: R.drawable.image_placeholder)
    }
}

private const val CHROME_PACKAGE = "com.android.chrome"

fun Context.openCustomTabs(uri: Uri, customTabsBuilder: CustomTabsIntent.Builder) {
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
        //todo timber
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
