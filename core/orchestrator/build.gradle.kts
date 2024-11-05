plugins {
    alias(libs.plugins.project.android.library)
    alias(libs.plugins.project.hilt)

}

android {
    namespace = "ai.looqs.android.core.orchestrator"
}

dependencies {
    implementation(libs.hilt.coroutines)
    implementation(libs.okhttp)

    implementation(projects.core.data)
    implementation(projects.core.accessibility)
    implementation(projects.core.voice)
    implementation(projects.core.llm)
    implementation(projects.core.screenshot)
    implementation(projects.core.reflector)
}
