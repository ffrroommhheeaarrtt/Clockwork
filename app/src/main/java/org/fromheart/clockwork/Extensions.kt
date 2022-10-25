package org.fromheart.clockwork

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

val Application.app: App
    get() = this as App

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

fun Context.getDaysLabel(hour: Int, minute: Int): String = getString(
    if (System.currentTimeMillis() >= getAlarmTime(hour, minute, false)) R.string.tomorrow else R.string.today
)

fun Context.getAlarmManager(): AlarmManager = ContextCompat.getSystemService(this, AlarmManager::class.java)!!

@SuppressLint("NewApi")
fun Context.isScheduleExactAlarmPermissionAllowed(): Boolean {
    return !(Build.VERSION.SDK_INT in Build.VERSION_CODES.S until Build.VERSION_CODES.TIRAMISU &&
            !getAlarmManager().canScheduleExactAlarms())
}

fun Context.isDarkTheme(): Boolean = (this.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        == Configuration.UI_MODE_NIGHT_YES)

@Suppress("DEPRECATION")
fun Activity.showOnLockScreen() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) {
        window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            addFlags(WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON)
            addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
            addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        }
    }
}

fun Activity.hideSystemBars() {
    WindowCompat.getInsetsController(window, window.decorView).apply {
        systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        hide(WindowInsetsCompat.Type.systemBars())
    }
}

