package com.burootro.mailio.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.burootro.mailio.data.prefs.MailioPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface StartDestination {
    data object Loading : StartDestination
    data object SignIn : StartDestination
    data object Home : StartDestination
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val prefs: MailioPreferences
) : ViewModel() {

    private val _destination = MutableStateFlow<StartDestination>(StartDestination.Loading)
    val destination: StateFlow<StartDestination> = _destination.asStateFlow()

    init {
        viewModelScope.launch {
            val key = prefs.getRecoveryKey()

            _destination.value = if (key != null) {
                StartDestination.Home
            } else {
                StartDestination.SignIn
            }
        }
    }
}
