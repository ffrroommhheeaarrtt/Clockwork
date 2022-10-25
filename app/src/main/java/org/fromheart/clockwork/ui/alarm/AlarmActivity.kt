package org.fromheart.clockwork.ui.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.launch
import org.fromheart.clockwork.*
import org.fromheart.clockwork.databinding.ActivityAlarmBinding
import org.fromheart.clockwork.receiver.AlarmReceiver

class AlarmActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlarmBinding

    private lateinit var localBroadcastManager: LocalBroadcastManager

    private val localReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_FINISH_ALARM_ACTIVITY) finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        localBroadcastManager = LocalBroadcastManager.getInstance(this).apply {
            registerReceiver(localReceiver, IntentFilter(ACTION_FINISH_ALARM_ACTIVITY))
        }

        showOnLockScreen()
        hideSystemBars()

        binding.apply {
            snoozeButton.setOnClickListener {
                sendBroadcast(Intent(applicationContext, AlarmReceiver::class.java).setAction(ACTION_SNOOZE_ALARM))
                finish()
            }
            stopButton.setOnClickListener {
                sendBroadcast(Intent(applicationContext, AlarmReceiver::class.java).setAction(ACTION_STOP_ALARM))
                finish()
            }

            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    currentTimeFlow.collect { timeText.text = it }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        localBroadcastManager.unregisterReceiver(localReceiver)
    }
}