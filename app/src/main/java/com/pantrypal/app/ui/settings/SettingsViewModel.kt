package com.pantrypal.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pantrypal.app.data.session.HouseholdSession
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val session: HouseholdSession) : ViewModel() {

    val householdName: StateFlow<String?> = session.householdName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun leaveHousehold() {
        viewModelScope.launch { session.leaveHousehold() }
    }
}
