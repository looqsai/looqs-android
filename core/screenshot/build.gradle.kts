plugins {
    alias(libs.plugins.project.android.library)
    alias(libs.plugins.project.hilt)

}

android {
    namespace = "ai.looqs.android.core.screenshot"
}

dependencies {
    implementation(libs.hilt.coroutines)
    implementation(libs.hilt.android.testing)
}
