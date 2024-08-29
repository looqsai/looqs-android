package com.sean8.jeminai.feature.settings

import com.sean8.jeminai.core.model.DarkThemeConfig.DARK
import com.sean8.jeminai.core.testing.repository.TestUserDataRepository
import com.sean8.jeminai.core.testing.util.MainDispatcherRule
import com.sean8.jeminai.feature.settings.SettingsUiState.Loading
import com.sean8.jeminai.feature.settings.SettingsUiState.Success
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private var userDataRepository = TestUserDataRepository()

    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        viewModel = SettingsViewModel(userDataRepository)
    }

    @Test
    fun stateIsInitiallyLoading() = runTest {
        assertEquals(Loading, viewModel.settingsUiState.value)
    }

    @Test
    fun stateIsSuccessAfterUserDataLoaded() = runTest {
        val collectJob =
            launch(UnconfinedTestDispatcher()) { viewModel.settingsUiState.collect() }

        userDataRepository.setDarkThemeConfig(DARK)

        assertEquals(
            Success(
                UserEditableSettings(
                    darkThemeConfig = DARK,
                ),
            ),
            viewModel.settingsUiState.value,
        )

        collectJob.cancel()
    }
}