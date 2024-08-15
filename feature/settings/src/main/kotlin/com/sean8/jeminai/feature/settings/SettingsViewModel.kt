package com.sean8.jeminai.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sean8.jeminai.feature.setting.respository.UserDataRepository
import com.sean8.jeminai.feature.settings.SettingsUiState.Loading
import com.sean8.jeminai.feature.settings.SettingsUiState.Success
import com.sean8.jeminai.feature.settings.model.data.DarkThemeConfig
import com.sean8.jeminai.feature.settings.model.data.DarkThemeConfig.FOLLOW_SYSTEM
import com.sean8.jeminai.feature.settings.model.data.UserData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userDataRepository: MockUserDataRepository,
) : ViewModel() {
    val settingsUiState: StateFlow<SettingsUiState> =
        userDataRepository.userData
            .map { userData ->
                Success(
                    UserEditableSettings(
                        darkThemeConfig = userData.darkThemeConfig,
                    )
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = WhileSubscribed(5.seconds.inWholeMilliseconds),
                initialValue = Loading,
            )

    fun updateDarkThemeConfig(darkThemeConfig: DarkThemeConfig) {
        viewModelScope.launch {
            userDataRepository.setDarkThemeConfig(darkThemeConfig)
        }
    }
}

data class UserEditableSettings(
    val darkThemeConfig: DarkThemeConfig,
)

sealed interface SettingsUiState {
    data object Loading : SettingsUiState
    data class Success(val settings: UserEditableSettings) : SettingsUiState
}

// TODO: Replace mock with actual data implementation
class MockUserDataRepository @Inject constructor() : UserDataRepository {
    private val _userData = MutableStateFlow(UserData(darkThemeConfig = FOLLOW_SYSTEM))
    override val userData: StateFlow<UserData> = _userData

    override suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig) {
        _userData.value = _userData.value.copy(darkThemeConfig = darkThemeConfig)
    }
}