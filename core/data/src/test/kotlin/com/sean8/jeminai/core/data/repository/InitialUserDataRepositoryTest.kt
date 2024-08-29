package com.sean8.jeminai.core.data.repository

import com.sean8.jeminai.core.datastore.AppPreferencesDataSource
import com.sean8.jeminai.core.datastore.UserPreferencesSerializer
import com.sean8.jeminai.core.model.DarkThemeConfig.DARK
import com.sean8.jeminai.core.model.DarkThemeConfig.FOLLOW_SYSTEM
import com.sean8.jeminai.core.testing.util.MainDispatcherRule
import com.sean8.jeminai.core.testing.util.testDataStore
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class InitialUserDataRepositoryTest {

    @get:Rule
    val mainUnconfinedDispatcher = MainDispatcherRule()

    @get:Rule
    val tmpFolder: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

    private lateinit var subject: InitialUserDataRepository

    private lateinit var appPreferenceDataSource: AppPreferencesDataSource

    @Before
    fun setup() {
        appPreferenceDataSource = AppPreferencesDataSource(
            tmpFolder.testDataStore(
                TestScope(mainUnconfinedDispatcher.testDispatcher),
                UserPreferencesSerializer(),
            ),
        )

        subject = InitialUserDataRepository(appPreferenceDataSource)
    }

    @Test
    fun testInitInitialUserDataRepository() = runTest {
        val userData = subject.userData.first()
        assertTrue(userData.darkThemeConfig == FOLLOW_SYSTEM)
    }

    @Test
    fun testModifyUserDataRepository() = runTest {
        subject.setDarkThemeConfig(DARK)
        with(subject.userData.first()) {
            assertTrue(darkThemeConfig == DARK)
        }
    }
}
