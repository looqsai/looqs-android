plugins {
    alias(libs.plugins.project.android.library)
}

android {
    namespace = "ai.looqs.android.core.model"
}
dependencies {
    api(libs.kotlinx.datetime)
}
