package org.fromheart.clockwork.util

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import org.fromheart.clockwork.R
import java.util.*

fun Int.hoursToMillis(): Long = this * HOUR_IN_MILLIS
fun Int.minutesToMillis(): Long = this * MINUTE_IN_MILLIS
fun Int.secondsToMillis(): Long = this * SECOND_IN_MILLIS

fun Long.millisToHours(): Int = (this / HOUR_IN_MILLIS).toInt()
fun Long.millisToMinutes(): Int = (this % HOUR_IN_MILLIS / MINUTE_IN_MILLIS).toInt()
fun Long.millisToSeconds(): Int = (this % HOUR_IN_MILLIS % MINUTE_IN_MILLIS / SECOND_IN_MILLIS).toInt()

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(PREFERENCES_DATA_STORE_NAME)

val Context.alarmManager: AlarmManager
    get() = getSystemService(Context.ALARM_SERVICE) as AlarmManager

val Context.notificationManager: NotificationManager
    get() = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

val Context.inputMethodManager: InputMethodManager
    get() = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

fun Context.showShortToast(text: String = "Toast") = Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
fun Context.showLongToast(text: String = "Toast") = Toast.makeText(this, text, Toast.LENGTH_LONG).show()

fun Context.getDaysLabel(hour: Int, minute: Int): String = getString(
    if (System.currentTimeMillis() >= getAlarmTime(hour, minute, false)) R.string.tomorrow else R.string.today
)

@SuppressLint("NewApi")
fun Context.isScheduleExactAlarmPermissionAllowed(): Boolean {
    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) true
    else alarmManager.canScheduleExactAlarms()
}

fun Context.isDarkTheme(): Boolean {
    return (this.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES)
}


fun Context.getFormattedClockDate(time: Long): String = Calendar.getInstance().run {
    timeInMillis = time
    val weekArray = resources.getStringArray(R.array.week_abb)
    val monthsArray = resources.getStringArray(R.array.months)
    "${weekArray[dayOfWeek]}, ${monthsArray[get(Calendar.MONTH)]} ${get(Calendar.DAY_OF_MONTH)}"
}

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

@Suppress("DEPRECATION")
fun Service.stopForeground() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) stopForeground(Service.STOP_FOREGROUND_REMOVE)
    else stopForeground(true)
}

fun RecyclerView.disableSimpleItemAnimator() {
    (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
}
