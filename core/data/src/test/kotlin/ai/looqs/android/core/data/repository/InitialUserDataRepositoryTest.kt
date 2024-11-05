package ai.looqs.android.core.data.repository

import ai.looqs.android.core.data.repository.InitialUserDataRepository
import ai.looqs.android.core.datastore.AppPreferencesDataSource
import ai.looqs.android.core.datastore.UserPreferencesSerializer
import ai.looqs.android.core.model.DarkThemeConfig.DARK
import ai.looqs.android.core.model.DarkThemeConfig.FOLLOW_SYSTEM
import ai.looqs.android.core.testing.util.MainDispatcherRule
import ai.looqs.android.core.testing.util.testDataStore
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
