package net.kebernet.worldengine

import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.zip.ZipFile

open class InstallTerraform: DefaultTask() {

    @Input
    var installTerraform = true

    @Input
    var terraformVersion = "0.12.3"

    @Input
    @Optional
    var terraformDownloadUrl: String? = null


    @Internal
    val urlTemplates = hashMapOf(
            Os.FAMILY_WINDOWS to "https://releases.hashicorp.com/terraform/{}/terraform_{}_windows_amd64.zip",
            Os.FAMILY_MAC to "https://releases.hashicorp.com/terraform/{}/terraform_{}_darwin_amd64.zip",
            Os.FAMILY_UNIX to "https://releases.hashicorp.com/terraform/{}/terraform_{}_linux_amd64.zip"
    )


    @TaskAction
    fun apply() {
        val targetFolder = File(project.rootProject.rootDir, ".gradle/terraform")
        if(!installTerraform || targetFolder.exists()) {
            return
        }
        val url = terraformDownloadUrl
                ?: urlTemplates.entries
                        .filter { p -> Os.isFamily(p.key) }
                        .map { p -> p.value }
                        .firstOrNull()
                        ?.replace("{}", terraformVersion)
                ?: throw RuntimeException("Unknown download URL. Please set the 'terraformDownloadUrl' property")
        logger.lifecycle("Downloading Terrafrom from $url")

        val tmpFile = File.createTempFile("terraform", "zip")
        tmpFile.deleteOnExit()
        URL(url).openStream().use { i ->
            FileOutputStream(tmpFile).use { o ->
                i.copyTo(o)
            }
        }
        unzip(tmpFile, targetFolder)
        if(Os.isFamily(Os.FAMILY_UNIX) || Os.isFamily(Os.FAMILY_MAC)){
            File(targetFolder, "terraform").setExecutable(true)
        }
    }

    fun unzip(zipFile: File, destinationDirectory: File) {
        logger.lifecycle("Extracting ${zipFile.absolutePath} to ${destinationDirectory.absolutePath}")
        ZipFile(zipFile).use { zip ->
            zip.entries().asSequence().forEach { entry ->
                zip.getInputStream(entry).use { input ->
                    val target = File(destinationDirectory, entry.name)
                    target.parentFile.mkdirs()
                    target.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
        }
    }


}