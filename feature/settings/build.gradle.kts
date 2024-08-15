plugins {
    alias(libs.plugins.project.android.feature)
    alias(libs.plugins.project.android.library.compose)
}

android {
    namespace = "com.sean8.jeminai.feature.settings"

    defaultConfig.testInstrumentationRunner = "com.sean8.jeminai.core.testing.HiltTestRunner"
}

dependencies {
    implementation(project(":core:testing"))

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.material3.android)
    implementation(libs.androidx.datastore.core.android)
    implementation(libs.core)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    androidTestImplementation(libs.bundles.androidx.compose.ui.test)
    androidTestImplementation(libs.hilt.android.testing)
}