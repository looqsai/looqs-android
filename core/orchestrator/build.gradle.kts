plugins {
    alias(libs.plugins.project.android.library)
    alias(libs.plugins.project.hilt)

}

android {
    namespace = "com.sean8.jeminai.core.orchestrator"
}

dependencies {
    implementation(libs.hilt.coroutines)

    implementation(projects.core.accessibility)
    implementation(projects.core.voice)
    implementation(projects.core.llm)
    implementation(projects.core.screenshot)
}
