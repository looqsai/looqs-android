plugins {
    alias(libs.plugins.project.android.library)
    alias(libs.plugins.project.hilt)

}

android {
    namespace = "com.sean8.jeminai.core.orchestrator"

//    // Ensure that assets are included
//    sourceSets {
//        getByName("main") {
//            assets.srcDirs("src/main/res/assets")
//        }
//    }
}

dependencies {
    implementation(libs.hilt.coroutines)
    implementation(projects.core.data)

    implementation(projects.core.accessibility)
    implementation(projects.core.voice)
    implementation(projects.core.llm)
    implementation(projects.core.screenshot)
}
