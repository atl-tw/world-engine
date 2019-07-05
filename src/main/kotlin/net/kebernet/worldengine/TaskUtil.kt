package net.kebernet.worldengine

import org.gradle.api.logging.Logger
import java.io.File
import java.util.*


fun doLogFile(terraformLog: File, logger: Logger, logTerraformOutput: Boolean, failOnTerraformErrors: Boolean, action: String) {
    val sb = StringBuilder()
    terraformLog.forEachLine { line ->
        if (logTerraformOutput) logger.lifecycle("Terraform[$action]: $line")
        if (failOnTerraformErrors && line.contains("Error: ")) sb.append(line)
        if (sb.isNotEmpty()) sb.append(line)
    }
    if(sb.isNotEmpty()) throw RuntimeException("Terraform failed with\n$sb")
}

fun findConfiguration(sourceDir: File, environment:String, suffix: String): List<File> {
    if (!sourceDir.exists() || !sourceDir.isDirectory) {
        throw RuntimeException("${sourceDir.absolutePath} isn't a directory")
    }
    val result: ArrayList<File> = ArrayList()
    var files = File(sourceDir, TerraformTask.ENVIRONMENTS_DIR)
            .listFiles { _, name ->
                name == "$environment.tfvars" }
    Optional.ofNullable(files).ifPresent { f -> result.addAll(f) }
    files = File(sourceDir, TerraformTask.ENVIRONMENTS_DIR)
            .listFiles { _, name -> name == "$environment-$suffix.tfvars" }
    Optional.ofNullable(files).ifPresent { f -> result.addAll(f) }
    return result
}