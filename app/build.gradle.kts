plugins {
    alias(libs.plugins.project.android.application)
    alias(libs.plugins.project.android.application.compose)
    alias(libs.plugins.project.hilt)
}

android {
    namespace = "ai.looqs.android"
    defaultConfig {
        applicationId = "ai.looqs.android"
        versionCode = 1
        versionName = "0.0.1"
        testInstrumentationRunner = "ai.looqs.android.core.testing.HiltTestRunner"
    }

    buildTypes {
        debug {
//            applicationIdSuffix = ".debug"
        }
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }

    packaging {
        resources {
            excludes.add("/META-INF/{AL2.0,LGPL2.1}")
        }
    }
}

dependencies {
    implementation(projects.feature.settings)
    implementation(projects.feature.setup)
    implementation(projects.feature.trigger)
    implementation(projects.core.testing)
    implementation(projects.core.orchestrator)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.runner)
    implementation(libs.androidx.ui.text.google.fonts)
    implementation(project(":core:util"))

    androidTestImplementation(libs.androidx.test.espresso.core)
}