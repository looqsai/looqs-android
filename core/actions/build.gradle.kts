plugins {
    alias(libs.plugins.project.android.library)
    alias(libs.plugins.project.hilt)

}

android {
    namespace = "ai.looqs.android.core.actions"
}

dependencies {
    api(libs.moshi.adapters)
    implementation(libs.moshi)
    implementation(libs.moshi.reflection)
    implementation(libs.moshi.codegen)
    ksp(libs.moshi.codegen)
}
