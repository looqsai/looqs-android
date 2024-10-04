plugins {
    alias(libs.plugins.project.android.library)
    alias(libs.plugins.project.hilt)

}

android {
    namespace = "com.sean8.jeminai.core.actions"
}

dependencies {
    api(libs.moshi.adapters)
    implementation(libs.moshi)
    implementation(libs.moshi.reflection)
    implementation(libs.moshi.codegen)
    ksp(libs.moshi.codegen)

}
