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