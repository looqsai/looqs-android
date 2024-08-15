import com.android.build.gradle.LibraryExtension
import com.sean8.jeminai.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply {
                apply("project.android.library")
                apply("project.hilt")
            }
            extensions.configure<LibraryExtension> {
                testOptions.animationsDisabled = true
            }

            dependencies {
                add("implementation", libs.findLibrary("androidx.hilt.navigation.compose").get())
                add("implementation", libs.findLibrary("androidx.lifecycle.runtimeCompose").get())
                add("implementation", libs.findLibrary("androidx.lifecycle.viewModelCompose").get())

                add("androidTestImplementation", libs.findLibrary("androidx.lifecycle.runtimeTesting").get())
            }
        }
    }
}
