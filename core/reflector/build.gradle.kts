plugins {
    alias(libs.plugins.project.android.library)
    alias(libs.plugins.project.hilt)
}

android {
    namespace = "com.sean8.jeminai.core.reflector"
}

dependencies {
    // OkHttp for WebSocket communication
    implementation(libs.okhttp)

    // Moshi for JSON serialization/deserialization
    api(libs.moshi.adapters)
    implementation(libs.moshi)
    implementation(libs.moshi.reflection)
    implementation(libs.moshi.codegen)
    ksp(libs.moshi.codegen)

    // Unit Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.kotlinx.coroutines.test)

    // Android Instrumentation Testing
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
}
