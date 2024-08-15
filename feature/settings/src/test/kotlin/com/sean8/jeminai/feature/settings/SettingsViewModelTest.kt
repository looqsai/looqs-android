package com.sean8.jeminai.feature.settings

import com.sean8.jeminai.feature.settings.SettingsUiState.Loading
import com.sean8.jeminai.feature.settings.SettingsUiState.Success
import com.sean8.jeminai.feature.settings.model.data.DarkThemeConfig.DARK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import kotlin.test.assertEquals

class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // TODO: Create test instead of mock
    private val userDataRepository = MockUserDataRepository()

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

/**
 * A JUnit [TestRule] that sets the Main dispatcher to [testDispatcher]
 * for the duration of the test.
 */
class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(testDispatcher)

    override fun finished(description: Description) = Dispatchers.resetMain()
}