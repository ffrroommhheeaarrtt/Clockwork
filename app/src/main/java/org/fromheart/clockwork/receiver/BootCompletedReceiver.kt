package org.fromheart.clockwork.receiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.fromheart.clockwork.data.AppDatabase
import org.fromheart.clockwork.repository.AlarmRepository

class BootCompletedReceiver : BroadcastReceiver() {

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {
        CoroutineScope(Job()).launch {
            val alarmRepository = AlarmRepository(AppDatabase.getDatabase(context).alarmDao())
            alarmRepository.updateTime(context)
        }
    }
}