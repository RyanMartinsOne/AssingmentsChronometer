package com.martins.assignmentschronometer

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.martins.assignmentschronometer.viewmodel.SettingsViewModel
import com.martins.assignmentschronometer.viewmodel.SharedViewModel
import com.martins.assignmentschronometer.viewmodel.WeeklyPartsViewModel

class App : Application(), ViewModelStoreOwner {

    private val appViewModelStore = ViewModelStore()
    override val viewModelStore: ViewModelStore get() = appViewModelStore

    private val factory by lazy {
        ViewModelProvider.AndroidViewModelFactory.getInstance(this)
    }

    val sharedViewModel: SharedViewModel by lazy {
        ViewModelProvider(this, factory)[SharedViewModel::class.java]
    }

    val weeklyPartsViewModel: WeeklyPartsViewModel by lazy {
        ViewModelProvider(this, factory)[WeeklyPartsViewModel::class.java]
    }

    val settingsViewModel: SettingsViewModel by lazy {
        ViewModelProvider(this, factory)[SettingsViewModel::class.java]
    }
}