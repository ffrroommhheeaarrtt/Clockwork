package org.fromheart.clockwork.di

import org.fromheart.clockwork.data.AppDatabase
import org.fromheart.clockwork.data.repository.AlarmRepository
import org.fromheart.clockwork.data.repository.StopwatchRepository
import org.fromheart.clockwork.data.repository.TimerRepository
import org.fromheart.clockwork.ui.viewmodel.AlarmViewModel
import org.fromheart.clockwork.ui.viewmodel.StopwatchViewModel
import org.fromheart.clockwork.ui.viewmodel.TimerViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    single { AlarmRepository(AppDatabase.getDatabase(get()).alarmDao()) }
    single { TimerRepository(AppDatabase.getDatabase(get()).timerDao()) }
    single { StopwatchRepository(AppDatabase.getDatabase(get()).stopwatchDao()) }
    viewModelOf(::AlarmViewModel)
    viewModelOf(::TimerViewModel)
    viewModelOf(::StopwatchViewModel)
}