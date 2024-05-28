package global.covesa.gradle

import org.gradle.api.Project

abstract class ApplicationPlugin : AndroidPlugin() {
    override fun apply(project: Project) {
        project.pluginManager.apply {
            apply("com.android.application")
        }

        super.apply(project)
    }
}
