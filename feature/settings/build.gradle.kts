plugins {
    alias(libs.plugins.project.android.feature)
    alias(libs.plugins.project.android.library.compose)
}

android {
    namespace = "ai.looqs.android.feature.settings"
}

dependencies {
    implementation(projects.core.testing)

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.material3.android)
    implementation(libs.androidx.datastore.core.android)
    implementation(libs.core)

    implementation(projects.core.data)

    androidTestImplementation(libs.bundles.androidx.compose.ui.test)
}