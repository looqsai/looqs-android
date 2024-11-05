package ai.looqs.android.feature.settings

import ai.looqs.android.core.model.DarkThemeConfig.DARK
import ai.looqs.android.core.testing.repository.TestUserDataRepository
import ai.looqs.android.core.testing.util.MainDispatcherRule
import ai.looqs.android.feature.settings.SettingsUiState.Loading
import ai.looqs.android.feature.settings.SettingsUiState.Success
import ai.looqs.android.feature.settings.SettingsViewModel
import ai.looqs.android.feature.settings.UserEditableSettings
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