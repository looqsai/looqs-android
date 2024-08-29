plugins {
    alias(libs.plugins.project.android.library)
    alias(libs.plugins.project.hilt)
}

android {
    defaultConfig {
        consumerProguardFiles("consumer-proguard-rules.pro")
    }
    namespace = "com.sean8.jeminai.core.datastore"
    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
    }
}

dependencies {
    api(projects.core.datastoreProto)
    api(projects.core.model)
    implementation(projects.core.dispatchers)

    api(libs.androidx.dataStore)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(project(":core:testing"))
}
