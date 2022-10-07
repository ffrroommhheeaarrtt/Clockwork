package org.fromheart.clockwork

import android.app.PendingIntent
import android.os.Build

const val TAG = "clockwork_tag"

val FLAG_IMMUTABLE: Int
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0

const val ALARM_CHANNEL_ID = "alarm"
const val ALARM_ID = 1

const val ACTION_STOP_ALARM = "clockwork_stop_alarm"
const val ACTION_FINISH_ALARM_ACTIVITY = "clockwork_finish_alarm_activity"

const val PERMISSION_REQUEST_POST_NOTIFICATIONS = 0

const val PREFERENCES_KEY_LAST_LOGIN_DATE = "last_login_date"
