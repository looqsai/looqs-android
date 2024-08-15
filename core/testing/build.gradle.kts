plugins {
    alias(libs.plugins.project.android.library)
    alias(libs.plugins.project.hilt)
}

android {
    namespace = "com.sean8.jeminai.core.testing"
}

dependencies {
    api(libs.kotlinx.coroutines.test)

    implementation(libs.androidx.test.rules)
    implementation(libs.hilt.android.testing)
    implementation(libs.kotlinx.datetime)
}