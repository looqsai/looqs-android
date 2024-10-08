plugins {
    alias(libs.plugins.project.android.library)
    alias(libs.plugins.project.hilt)

}

android {
    namespace = "com.sean8.jeminai.core.screenshot"
}

dependencies {
    implementation(libs.hilt.coroutines)
    implementation(libs.hilt.android.testing)
}
