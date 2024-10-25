plugins {
    id("global.covesa.library")
}

android {
    namespace = "global.covesa.sdk.api.client"

    buildFeatures.aidl = true
}

dependencies {
    api(project(":api:aidl"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    api(libs.unifiedpush)
}
