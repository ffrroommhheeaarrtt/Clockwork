package org.fromheart.clockwork

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Application
import android.content.Context
<<<<<<< HEAD
import android.content.res.Configuration
=======
>>>>>>> 602fd28e42c73f1c964c6a01bbbc21ca2e05e3dd
import android.os.Build
import androidx.core.content.ContextCompat
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
<<<<<<< HEAD
}

fun Context.isDarkTheme(): Boolean = (this.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        == Configuration.UI_MODE_NIGHT_YES)
=======
}
>>>>>>> 602fd28e42c73f1c964c6a01bbbc21ca2e05e3dd
