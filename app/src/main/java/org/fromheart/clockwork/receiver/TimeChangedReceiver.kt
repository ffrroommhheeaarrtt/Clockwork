package org.fromheart.clockwork.receiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.fromheart.clockwork.App

class TimeChangedReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(SupervisorJob())

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {
        scope.launch {
            val alarmRepository = (context.applicationContext as App).alarmRepository
            alarmRepository.updateTime(context)
        }
    }
}