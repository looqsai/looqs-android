import java.util.Properties

plugins {
    alias(libs.plugins.project.android.library)
    alias(libs.plugins.project.hilt)

}

android {
    namespace = "ai.looqs.android.core.llm"

    buildFeatures.buildConfig = true
    defaultConfig.buildConfigField("String", "OPENAI_API_KEY", "\"${getOpenAiApiKey()}\"")
}

fun getOpenAiApiKey(): String {
    val properties = Properties()
    val localPropertiesFile = project.rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        properties.load(localPropertiesFile.inputStream())
        return properties.getProperty("OPENAI_API_KEY", "mock_api_key")
    }
    return "mock_api_key"
}

dependencies {
    implementation(libs.hilt.coroutines)
    implementation(libs.androidx.recyclerview)
    implementation(libs.okhttp)
    implementation(libs.commons.codec)
    implementation(libs.androidx.foundation.android)
    implementation(libs.androidx.tracing.ktx)
    implementation(libs.androidx.lifecycle.viewModelCompose)
    implementation(libs.androidx.material3.android)
    implementation(libs.hilt.coroutines)

    api(projects.core.actions)

    api(libs.moshi.adapters)
    implementation(libs.moshi)
    implementation(libs.moshi.reflection)
    implementation(libs.moshi.codegen)
    implementation(project(":core:util"))
    ksp(libs.moshi.codegen)

    testImplementation(libs.okio)
    testImplementation(libs.mockwebserver)
}
