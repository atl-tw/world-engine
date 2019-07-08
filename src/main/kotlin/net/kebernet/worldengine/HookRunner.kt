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

import java.io.File

class HookRunner  {

    val environmentState = HashMap<String, String>()

    init {
        this.environmentState.putAll(System.getenv())
    }

    fun executeCommand(command: List<String>, workingDirectory: File?, log: File): Int {
        val pb = ProcessBuilder()
        pb.environment().putAll(this.environmentState)
        val result = pb
                .directory(workingDirectory ?: File("."))
                .command(command)
                .redirectError(log)
                .redirectOutput(log)
                .start()
                .waitFor()

        if(result != 0){
            throw RuntimeException("Failed to execute ($result) ${command.joinToString(" ")}, see log at ${log.absolutePath}")
        }


        return result
    }


    fun execute(scriptFile: File, workingDirectory: File?, log: File): Int {
        val tempFile = File.createTempFile("env", "txt")
        tempFile.deleteOnExit()
        val command = listOf(
                "bash", "-c", "source ${scriptFile.absolutePath}; env > ${tempFile.absolutePath}"
        )
        val result = executeCommand(command, workingDirectory ?: scriptFile.parentFile, log)
        tempFile.forEachLine {
            line ->
            val key = line.substring(0, line.indexOf('='))
            val value = line.substring(line.indexOf('=') + 1)

            this.environmentState[key] = value
        }

        return result
    }

}