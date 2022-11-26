package org.fromheart.clockwork.ui.screen.timer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.launch
import org.fromheart.clockwork.databinding.ActivityTimerBinding
import org.fromheart.clockwork.service.TimerService
import org.fromheart.clockwork.ui.viewmodel.TimerViewModel
import org.fromheart.clockwork.util.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class TimerActivity : AppCompatActivity() {

    private val viewModel: TimerViewModel by viewModel()

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
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.alertTimerTime.collect {
                        timerTimeText.text = getFormattedTimerTime(it)
                    }
                }
            }
            stopButton.setOnClickListener {
                startService(Intent(this@TimerActivity, TimerService::class.java).setAction(ACTION_STOP_ALERT_TIMER))
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        localBroadcastManager.unregisterReceiver(localReceiver)
    }
}