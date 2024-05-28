package global.covesa.gradle

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class ComposePlugin : Plugin<Project> {
    companion object {
        private const val STABLE_COMPILER_VERSION = "1.5.10"
    }

    override fun apply(project: Project) {
        project.extensions.getByType(CommonExtension::class.java).apply {
            buildFeatures.apply {
                compose = true
            }
            composeOptions.apply {
                kotlinCompilerExtensionVersion = STABLE_COMPILER_VERSION
            }
        }
    }
}
