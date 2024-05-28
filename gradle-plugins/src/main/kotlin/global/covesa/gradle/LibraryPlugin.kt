package global.covesa.gradle

import org.gradle.api.Project

class LibraryPlugin : AndroidPlugin() {
    override fun apply(project: Project) {
        project.pluginManager.apply {
            apply("com.android.library")
        }

        super.apply(project)
    }
}
