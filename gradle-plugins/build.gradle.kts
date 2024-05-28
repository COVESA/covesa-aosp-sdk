plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

gradlePlugin {
    // base android plugin with sdk and jvm versions
    plugins.register("global.covesa.android") {
        id = "global.covesa.android"
        implementationClass = "global.covesa.gradle.AndroidPlugin"
    }

    // uses the application plugin
    plugins.register("global.covesa.application") {
        id = "global.covesa.application"
        implementationClass = "global.covesa.gradle.ApplicationPlugin"
    }

    // uses the library plygin
    plugins.register("global.covesa.library") {
        id = "global.covesa.library"
        implementationClass = "global.covesa.gradle.LibraryPlugin"
    }

    // sets up compose build feature & compiler version
    plugins.register("global.covesa.compose") {
        id = "global.covesa.compose"
        implementationClass = "global.covesa.gradle.ComposePlugin"
    }
}

dependencies {
    compileOnly(libs.kotlinGradlePlugin)
    compileOnly(libs.agpGradleApi)
}
