plugins {
    alias(libs.plugins.project.android.feature)
    alias(libs.plugins.project.android.library.compose)

}

android {
    namespace = "com.sean8.jeminai.core.accessibility"
}

dependencies {
//    implementation(libs.hilt.coroutines)
    implementation(libs.moshi)
    implementation(libs.moshi.reflection)
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
    implementation(projects.core.data)

    implementation(libs.moshi.adapters)
    implementation(libs.moshi.codegen)
    ksp(libs.moshi.codegen)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.kotlinx.serialization.json)
    testImplementation(libs.hilt.android.testing)
    testImplementation(libs.hilt.coroutines)
    testImplementation(libs.robolectric)
    testImplementation(projects.core.testing)

    androidTestImplementation(libs.bundles.androidx.compose.ui.test)
}
