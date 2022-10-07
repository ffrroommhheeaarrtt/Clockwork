package org.fromheart.clockwork.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import androidx.activity.viewModels
import com.google.android.material.snackbar.Snackbar
import org.fromheart.clockwork.*
import org.fromheart.clockwork.databinding.ActivityMainBinding
import org.fromheart.clockwork.receiver.BootCompletedReceiver
import org.fromheart.clockwork.viewmodel.MainViewModel
import org.fromheart.clockwork.viewmodel.MainViewModelFactory

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(application.app.database)
    }

    private lateinit var binding: ActivityMainBinding

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val alarmChannel = NotificationChannel(
                ALARM_CHANNEL_ID,
                getString(R.string.channel_name_alarm),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
                    AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build()
                )
            }

            getSystemService(NotificationManager::class.java).apply {
                createNotificationChannel(alarmChannel)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        createNotificationChannels()

        registerReceiver(BootCompletedReceiver(), IntentFilter(Intent.ACTION_BOOT_COMPLETED))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), PERMISSION_REQUEST_POST_NOTIFICATIONS)
        }
    }

    @SuppressLint("InlinedApi")
    override fun onStart() {
        super.onStart()
        if (Build.VERSION.SDK_INT in Build.VERSION_CODES.S until Build.VERSION_CODES.TIRAMISU) {
            if (getAlarmManager().canScheduleExactAlarms()) viewModel.setAlarm(this)
            else {
                Snackbar.make(binding.root, R.string.snackbar_schedule_exact_alarm_permission, Snackbar.LENGTH_LONG).apply {
                    setAction(R.string.snackbar_button_settings) {
                        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:$packageName")).let { intent ->
                            intent.addCategory(Intent.CATEGORY_DEFAULT)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        }
                    }
                    show()
                }
            }
        }
    }
}