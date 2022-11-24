package org.fromheart.clockwork.util

import android.app.PendingIntent
import android.os.Build

const val PACKAGE_NAME = "org.fromheart.clockwork"

const val HOUR_IN_MILLIS = 3600000L
const val MINUTE_IN_MILLIS = 60000L
const val SECOND_IN_MILLIS = 1000L

val FLAG_IMMUTABLE: Int
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0

const val ALARM_CHANNEL_ID = "alarm"
const val ALARM_ID = 101
const val SNOOZED_ALARM_ID = 102

const val TIMER_CHANNEL_ID = "timer"
const val TIMER_ID = 201
const val ALERT_TIMER_ID = 202
const val MISSED_TIMER_ID = 203

const val STOPWATCH_CHANNEL_ID = "stopwatch"
const val STOPWATCH_ID = 301

const val ACTION_ALARM_FRAGMENT = "${PACKAGE_NAME}_action_alarm_fragment"
const val ACTION_TIMER_FRAGMENT = "${PACKAGE_NAME}_action_timer_fragment"
const val ACTION_STOPWATCH_FRAGMENT = "${PACKAGE_NAME}_action_stopwatch_fragment"
const val ACTION_FINISH_ALARM_ACTIVITY = "${PACKAGE_NAME}_action_finish_alarm_activity"
const val ACTION_FINISH_TIMER_ACTIVITY = "${PACKAGE_NAME}_action_finish_timer_activity"
const val ACTION_SNOOZE_ALARM = "${PACKAGE_NAME}_action_snooze_alarm"
const val ACTION_STOP_ALARM = "${PACKAGE_NAME}_action_stop_alarm"
const val ACTION_PAUSE_TIMER = "${PACKAGE_NAME}_action_pause_timer"
const val ACTION_STOP_TIMER = "${PACKAGE_NAME}_action_stop_timer"
const val ACTION_STOP_ALERT_TIMER = "${PACKAGE_NAME}_action_stop_alert_timer"
const val ACTION_RESET_ALL_TIMERS = "${PACKAGE_NAME}_action_reset_all_timers"
const val ACTION_START_STOPWATCH = "${PACKAGE_NAME}_action_start_stopwatch"
const val ACTION_PAUSE_STOPWATCH = "${PACKAGE_NAME}_action_pause_stopwatch"
const val ACTION_STOP_STOPWATCH = "${PACKAGE_NAME}_action_stop_stopwatch"
const val ACTION_SET_STOPWATCH_FLAG = "${PACKAGE_NAME}_action_set_stopwatch_flag"

const val PERMISSION_REQUEST_POST_NOTIFICATIONS = 1

const val PREFERENCES_KEY_LAST_LOGIN_DATE = "last_login_date"
const val PREFERENCES_KEY_ALARM_TIME = "alarm_time"