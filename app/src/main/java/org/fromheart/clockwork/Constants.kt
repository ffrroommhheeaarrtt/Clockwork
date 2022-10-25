package org.fromheart.clockwork

import android.app.PendingIntent
import android.os.Build

const val TAG = "clockwork_tag"

val FLAG_IMMUTABLE: Int
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0

const val ALARM_CHANNEL_ID = "alarm"
const val ALARM_ID = 1
const val SNOOZED_ALARM_ID = 2

const val TIMER_CHANNEL_ID = "timer"
const val TIMER_ID = 3
const val ALERT_TIMER_ID = 4
const val MISSED_TIMER_ID = 5

const val ACTION_ALARM_FRAGMENT = "clockwork_alarm_fragment"
const val ACTION_TIMER_FRAGMENT = "clockwork_timer_fragment"
const val ACTION_SNOOZE_ALARM = "clockwork_snooze_alarm"
const val ACTION_STOP_ALARM = "clockwork_stop_alarm"
const val ACTION_PAUSE_TIMER = "clockwork_pause_timer"
const val ACTION_STOP_TIMER = "clockwork_stop_timer"
const val ACTION_FINISH_ALARM_ACTIVITY = "clockwork_finish_alarm_activity"
const val ACTION_FINISH_TIMER_ACTIVITY = "clockwork_finish_timer_activity"

const val PERMISSION_REQUEST_POST_NOTIFICATIONS = 0

const val PREFERENCES_KEY_LAST_LOGIN_DATE = "last_login_date"
const val PREFERENCES_KEY_ALARM_TIME = "alarm_time"