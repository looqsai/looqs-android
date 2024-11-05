plugins {
    alias(libs.plugins.project.android.library)
    alias(libs.plugins.protobuf)
}

android {
    namespace = "ai.looqs.android.core.datastore.proto"
}

// Setup protobuf configuration, generating lite Java and Kotlin classes
protobuf {
    protoc {
        artifact = libs.protobuf.protoc.get().toString()
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                register("java") {
                    option("lite")
                }
                register("kotlin") {
                    option("lite")
                }
            }
        }
    }
}


dependencies {
    api(libs.protobuf.kotlin.lite)
}
