/**
 * Copyright 2019 Robert Cooper, ThoughWorks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.kebernet.worldengine

import org.gradle.api.logging.Logger
import java.io.File
import java.util.Optional
import kotlin.collections.ArrayList

fun doLogFile(terraformLog: File, logger: Logger, logTerraformOutput: Boolean, failOnTerraformErrors: Boolean, action: String) {
    val sb = StringBuilder()
    terraformLog.forEachLine { line ->
        if (logTerraformOutput) logger.lifecycle("Terraform[$action]: $line")
        if (failOnTerraformErrors && line.contains("Error: ")) sb.append(line)
        if (sb.isNotEmpty()) sb.append(line)
    }
    if (sb.isNotEmpty()) throw RuntimeException("Terraform failed with\n$sb")
}

fun findConfiguration(sourceDir: File, environment: String, suffix: String): List<File> {
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