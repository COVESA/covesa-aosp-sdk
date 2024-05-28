// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    java
    kotlin("android") version libs.versions.kotlin apply false
    kotlin("jvm") version libs.versions.kotlin apply false
    kotlin("kapt") version libs.versions.kotlin apply false
    id("com.android.application") version libs.versions.androidGradlePlugin apply false
    id("com.android.library") version libs.versions.androidGradlePlugin apply false
}
