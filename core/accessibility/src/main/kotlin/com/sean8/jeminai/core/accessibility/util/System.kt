package com.sean8.jeminai.core.accessibility.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.preference.PreferenceManager
import android.view.LayoutInflater
import com.squareup.moshi.Json

fun now() = System.currentTimeMillis()

/** Workaround, read https://commonsware.com/blog/2017/04/11/android-o-implicit-broadcast-ban.html **/
fun sendImplicitBroadcast(context: Context, i: Intent) {
    val pm = context.packageManager as PackageManager
    val matches = pm.queryBroadcastReceivers(i, 0)

    for (receivingActivityInfo in matches.map { it.activityInfo }) {
        val explicit = Intent(i).apply {
            component = ComponentName(
                receivingActivityInfo.applicationInfo.packageName, receivingActivityInfo.name,
            )
        }
        context.sendBroadcast(explicit)
    }
}

fun Context.connectivityManager() =
    applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

fun Context.pref(): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

fun Context.getInstalledApps(): List<App> {
    val pm = this.packageManager
    val applicationsInfo = pm.getInstalledApplications(PackageManager.GET_META_DATA)

    val apps = mutableListOf<App>()

    for (appInfo in applicationsInfo) {
        apps.add(
            App(
                name = pm.getApplicationLabel(appInfo).toString(),
                packageName = appInfo.packageName,
                activity = pm?.getLaunchIntentForPackage(appInfo.packageName)?.component?.className
                    ?: continue,
            ),
        )
    }

    return apps
}

val Context.layoutInflater
    get() = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

class App(
    val name: String,
    val packageName: String,
    val activity: String,
) {
    @Json(name = "package")
    var packageAndActivity = "$packageName:$activity"
}