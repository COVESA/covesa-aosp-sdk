package global.covesa.gradle

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension

abstract class AndroidPlugin : Plugin<Project> {
    companion object {
        private const val MIN_SDK = 29
        private const val COMPILE_SDK = 34
        private const val BUILD_TOOLS = "34.0.0"
    }

    override fun apply(project: Project) {
        project.pluginManager.apply {
            apply("kotlin-android")
        }

        project.kotlinExtension.jvmToolchain(jdkVersion = 17)

        project.extensions.getByType(CommonExtension::class.java).apply {
            defaultConfig.apply {
                minSdk = MIN_SDK
                compileSdk = COMPILE_SDK
                buildToolsVersion = BUILD_TOOLS
            }

            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }

            packaging {
                resources {
                    excludes.add("/META-INF/{AL2.0,LGPL2.1}")
                }
            }
        }
    }
}
