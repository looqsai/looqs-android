plugins {
    alias(libs.plugins.project.android.library)
    alias(libs.plugins.project.hilt)
}

android {
    defaultConfig {
        consumerProguardFiles("consumer-proguard-rules.pro")
    }
    namespace = "ai.looqs.android.core.datastore"
    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
    }
}

dependencies {
    api(projects.core.datastoreProto)
    api(projects.core.model)
    implementation(libs.hilt.coroutines)

    api(libs.androidx.dataStore)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(projects.core.testing)
}
