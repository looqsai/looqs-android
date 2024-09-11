plugins {
    alias(libs.plugins.project.android.library)
    alias(libs.plugins.project.hilt)
}

android {
    namespace = "com.sean8.jeminai.core.data"
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }
}

dependencies {
    api(projects.core.datastore)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.kotlinx.serialization.json)
    testImplementation(projects.core.testing)
    testImplementation(libs.hilt.android.testing)
    testImplementation(libs.hilt.coroutines)
    testImplementation(libs.robolectric)

    testImplementation(projects.core.datastore)
}
