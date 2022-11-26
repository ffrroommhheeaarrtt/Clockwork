package org.fromheart.clockwork.receiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.fromheart.clockwork.data.repository.AlarmRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class TimeChangedReceiver : BroadcastReceiver(), KoinComponent {

    private val scope = CoroutineScope(SupervisorJob())

    private val alarmRepository: AlarmRepository by inject()

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {
        scope.launch {
            alarmRepository.updateTime(context)
        }
    }
}