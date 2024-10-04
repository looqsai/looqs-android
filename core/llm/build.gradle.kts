plugins {
    alias(libs.plugins.project.android.library)
    alias(libs.plugins.project.hilt)

}

android {
    namespace = "com.sean8.jeminai.core.llm"
}

dependencies {
    implementation(libs.hilt.coroutines)
    implementation(libs.androidx.recyclerview)
    implementation(libs.okhttp)
    implementation(libs.commons.codec)
    implementation(libs.androidx.foundation.android)
    implementation(libs.androidx.tracing.ktx)
    implementation(libs.androidx.lifecycle.viewModelCompose)
    implementation(libs.androidx.lifecycle.livedata)
    implementation(libs.androidx.compose.runtime.livedata)
    implementation(libs.androidx.material3.android)
    implementation(libs.hilt.coroutines)
    implementation(projects.core.actions)

    ksp(libs.moshi.codegen)

}
