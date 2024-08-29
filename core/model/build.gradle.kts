plugins {
    alias(libs.plugins.project.android.library)
}

android {
    namespace = "com.sean8.jeminai.core.model"
}
dependencies {
    api(libs.kotlinx.datetime)
}
