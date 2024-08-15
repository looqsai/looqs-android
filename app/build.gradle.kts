plugins {
    alias(libs.plugins.project.android.application)
    alias(libs.plugins.project.android.application.compose)
    alias(libs.plugins.project.hilt)
}

android {
    namespace = "com.sean8.jeminai"
    defaultConfig {
        applicationId = "com.sean8.jeminai"
        versionCode = 1
        versionName = "0.0.1"
        testInstrumentationRunner = "com.sean8.jeminai.core.testing.HiltTestRunner"
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
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
    implementation(project(":feature:settings"))
    implementation(project(":core:testing"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.runner)
}