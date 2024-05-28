plugins {
    id("global.covesa.application")
}

android {
    namespace = "global.covesa.sdk.server"
}

dependencies {
    implementation(project(":api:aidl"))
}
