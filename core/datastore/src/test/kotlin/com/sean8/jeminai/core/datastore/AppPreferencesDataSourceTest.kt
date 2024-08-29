import com.sean8.jeminai.core.datastore.AppPreferencesDataSource
import com.sean8.jeminai.core.datastore.UserPreferencesSerializer
import com.sean8.jeminai.core.model.DarkThemeConfig
import com.sean8.jeminai.core.testing.util.MainDispatcherRule
import com.sean8.jeminai.core.testing.util.testDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import kotlin.test.Test
import kotlin.test.assertEquals

class AppPreferencesDataSourceTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var subject: AppPreferencesDataSource

    @get:Rule
    val tmpFolder: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

    @Before
    fun setup() {
        subject = AppPreferencesDataSource(
            tmpFolder.testDataStore(TestScope(), UserPreferencesSerializer()),
        )
    }

    @Test
    fun initialDarkThemeConfigIsLight() = runTest {
        advanceUntilIdle()
        assertEquals(DarkThemeConfig.FOLLOW_SYSTEM, subject.userData.first().darkThemeConfig)
    }
}