package org.fromheart.clockwork.ui.timer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import org.fromheart.clockwork.*
import org.fromheart.clockwork.databinding.ActivityTimerBinding
import org.fromheart.clockwork.service.TimerService

class TimerActivity : AppCompatActivity() {

    private lateinit var localBroadcastManager: LocalBroadcastManager

    private val localReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_FINISH_TIMER_ACTIVITY) finish()
        }
    }

    private lateinit var binding: ActivityTimerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTimerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        localBroadcastManager = LocalBroadcastManager.getInstance(this).apply {
            registerReceiver(localReceiver, IntentFilter(ACTION_FINISH_TIMER_ACTIVITY))
        }

        showOnLockScreen()
        hideSystemBars()

        binding.apply {
            timerTimeText.text = intent.data.toString()
            stopButton.setOnClickListener {
                ContextCompat.startForegroundService(
                    applicationContext,
                    Intent(applicationContext, TimerService::class.java).setAction(ACTION_STOP_TIMER)
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        localBroadcastManager.unregisterReceiver(localReceiver)
    }
}