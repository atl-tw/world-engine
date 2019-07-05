package net.kebernet.worldengine

import org.gradle.api.Plugin
import org.gradle.api.Project

open class WorldEngine : Plugin<Project> {


    override fun apply(project: Project) {
        with(project) {
            project.extensions.extraProperties.set("WorldEngineTask", TerraformTask::class.java)
            project.extensions.extraProperties.set("InstallTerraform", InstallTerraform::class.java)
        }
    }

}