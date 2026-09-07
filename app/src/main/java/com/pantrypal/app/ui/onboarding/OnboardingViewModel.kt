package com.pantrypal.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pantrypal.app.data.model.Household
import com.pantrypal.app.data.repository.HouseholdRepository
import com.pantrypal.app.data.session.HouseholdSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OnboardingUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class OnboardingViewModel(
    private val householdRepository: HouseholdRepository,
    private val session: HouseholdSession
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState

    fun createHousehold(name: String, onDone: () -> Unit) {
        if (name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Give your household a name") }
            return
        }
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            runCatching { householdRepository.createHousehold(name.trim()) }
                .onSuccess { household: Household ->
                    session.setHousehold(household.id, household.name)
                    _uiState.update { it.copy(isLoading = false) }
                    onDone()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error.message ?: "Couldn't create household")
                    }
                }
        }
    }

    fun joinHousehold(code: String, onDone: () -> Unit) {
        if (code.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Enter the household code") }
            return
        }
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            runCatching { householdRepository.joinHousehold(code) }
                .onSuccess { household ->
                    if (household == null) {
                        _uiState.update {
                            it.copy(isLoading = false, errorMessage = "No household found with that code")
                        }
                    } else {
                        session.setHousehold(household.id, household.name)
                        _uiState.update { it.copy(isLoading = false) }
                        onDone()
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error.message ?: "Couldn't join household")
                    }
                }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
