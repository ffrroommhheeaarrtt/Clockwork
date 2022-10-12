package org.fromheart.clockwork.ui.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.launch
import org.fromheart.clockwork.ACTION_FINISH_ALARM_ACTIVITY
import org.fromheart.clockwork.currentTime
import org.fromheart.clockwork.databinding.ActivityAlarmBinding
import org.fromheart.clockwork.service.AlarmService
import org.fromheart.clockwork.snoozeAlarmService

class AlarmActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlarmBinding

    private lateinit var localBroadcastManager: LocalBroadcastManager
    private val localReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_FINISH_ALARM_ACTIVITY) finish()
        }
    }

    @Suppress("DEPRECATION")
    private fun showOnLockScreen() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) {
            window.apply {
                addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                addFlags(WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON)
                addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
                addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
            }
        }
    }

    private fun hideSystemBars() {
        WindowCompat.getInsetsController(window, window.decorView).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
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
                snoozeAlarmService()
                finish()
            }
            stopButton.setOnClickListener {
                AlarmService.stop()
                finish()
            }
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    currentTime.collect { timeText.text = it }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        localBroadcastManager.unregisterReceiver(localReceiver)
    }
}