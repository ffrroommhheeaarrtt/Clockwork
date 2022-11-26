package org.fromheart.clockwork.ui.screen.main

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
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.snackbar.Snackbar
import org.fromheart.clockwork.R
import org.fromheart.clockwork.databinding.ActivityMainBinding
import org.fromheart.clockwork.receiver.BootCompletedReceiver
import org.fromheart.clockwork.ui.viewmodel.AlarmViewModel
import org.fromheart.clockwork.util.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private val alarmViewModel: AlarmViewModel by viewModel()

    private lateinit var binding: ActivityMainBinding

    private lateinit var navController: NavController

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
            val timerChannel = NotificationChannel(
                TIMER_CHANNEL_ID,
                getString(R.string.channel_name_timer),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
                    AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build()
                )
            }
            val stopwatchChannel = NotificationChannel(
                STOPWATCH_CHANNEL_ID,
                getString(R.string.channel_name_stopwatch),
                NotificationManager.IMPORTANCE_DEFAULT
            )

            val channelList = listOf(alarmChannel, timerChannel, stopwatchChannel)
            notificationManager.createNotificationChannels(channelList)
        }
    }

    @SuppressLint("InlinedApi")
    private fun showSettingsSnackbar() {
        Snackbar.make(binding.root, R.string.snackbar_schedule_exact_alarm_permission, Snackbar.LENGTH_LONG).apply {
            setAction(R.string.snackbar_button_settings) {
                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:$PACKAGE_NAME")).let { intent ->
                    intent.addCategory(Intent.CATEGORY_DEFAULT)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }
            }
            show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        createNotificationChannels()

        registerReceiver(BootCompletedReceiver(), IntentFilter(Intent.ACTION_BOOT_COMPLETED))

        navController = (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController

        binding.bottomNavigation.setupWithNavController(navController)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), PERMISSION_REQUEST_POST_NOTIFICATIONS)
        }

        when (intent.action) {
            ACTION_ALARM_FRAGMENT -> navController.navigate(
                R.id.alarm_fragment,
                null,
                NavOptions.Builder().setPopUpTo(R.id.alarm_fragment, true).build()
            )
            ACTION_TIMER_FRAGMENT -> navController.navigate(
                R.id.timer_fragment,
                null,
                NavOptions.Builder().setPopUpTo(R.id.alarm_fragment, true).build()
            )
            ACTION_STOPWATCH_FRAGMENT -> navController.navigate(
                R.id.stopwatch_fragment,
                null,
                NavOptions.Builder().setPopUpTo(R.id.alarm_fragment, true).build()
            )
        }
        intent.action = null
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onStart() {
        super.onStart()

        if (isScheduleExactAlarmPermissionAllowed()) alarmViewModel.setAlarm()
        else showSettingsSnackbar()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
    }
}