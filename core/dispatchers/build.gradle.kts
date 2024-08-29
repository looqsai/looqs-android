plugins {
    alias(libs.plugins.project.android.library)
    alias(libs.plugins.project.hilt)
}

android.namespace = "com.sean8.jeminai.core.dispatchers"

dependencies {
    testImplementation(libs.hilt.android.testing)
}