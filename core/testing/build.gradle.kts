plugins {
    alias(libs.plugins.project.android.library)
    alias(libs.plugins.project.hilt)
}

android {
    namespace = "com.sean8.jeminai.core.testing"
}

dependencies {
    api(libs.kotlinx.coroutines.test)
    api(projects.core.dispatchers)
    api(projects.core.data)
    api(projects.core.model)

    implementation(libs.androidx.dataStore)
    api(libs.androidx.test.rules)
    implementation(libs.hilt.android.testing)
    implementation(libs.kotlinx.datetime)
}
