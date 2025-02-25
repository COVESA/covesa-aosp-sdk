plugins {
    id("global.covesa.application")
    id("global.covesa.compose")
}

android {
    namespace = "global.covesa.sdk.client"
}

dependencies {
    implementation(project(":api:client"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.material3)
    implementation(libs.kotlinxCollectionsImmutable)

    /** Used by the [FakeApplicationServer][global.covesa.sdk.client.push.FakeApplicationServer] */
    implementation(libs.volley)
    implementation(libs.tink.webpush)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
