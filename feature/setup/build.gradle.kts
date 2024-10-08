plugins {
    alias(libs.plugins.project.android.feature)
    alias(libs.plugins.project.android.library.compose)
}

android {
    namespace = "com.sean8.jeminai.feature.setup"
}

dependencies {
    implementation(projects.core.testing)
    implementation(projects.core.accessibility)
    implementation(projects.core.voice)
    implementation(projects.core.orchestrator)
    implementation(projects.core.screenshot)

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.material3.android)
    implementation(libs.androidx.datastore.core.android)

    implementation(libs.androidx.foundation.android)
    implementation(libs.androidx.tracing.ktx)

    // TODO: ??
    implementation(libs.core)

    implementation(projects.core.data)

    androidTestImplementation(libs.bundles.androidx.compose.ui.test)
}