package net.kebernet.worldengine

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import org.gradle.api.tasks.Optional
import java.io.File
import java.util.*
import java.util.Optional.ofNullable


@Suppress("MemberVisibilityCanBePrivate")
abstract class TerraformTask : DefaultTask() {

    companion object Constants {
        const val ENVIRONMENTS_DIR = "environments"
        const val DEFAULT_ENV = "dev"
        val DEFAULT_TF_ARGS = listOf("-no-color", "-input=false", "-force-copy", "-backend=true", "-reconfigure", "-upgrade")
    }

    @InputFile @Optional
    var terraformExecutable: File? = null

    @InputDirectory
    var terraformSourceDir: File = File(project.projectDir, "src/deploy/terraform")

    @Input
    var component = project.findProperty("we.component") as String?

    @Input
    var environment = project.findProperty("we.environment") as String? ?: DEFAULT_ENV

    @Input
    var version: String = project.findProperty("we.version") as String? ?: "undefined"

    @Input
    var action = "init"

    @Internal
    var files: List<File>? = null

    @Internal
    val logDir =  File(project.buildDir, "world-engine")

    @Internal
    protected lateinit var logFile: File

    @Internal
    protected lateinit var tfLog: File

    @Internal
    protected lateinit var workingDirectory: File

    @Internal
    private lateinit var runner: HookRunner


    fun findConfigurations(): List<File> {
        val result: ArrayList<File> = ArrayList()
        val componentDir = File(terraformSourceDir, "components/$component")
        result.addAll(scanDirectory(terraformSourceDir))
        result.addAll(scanDirectory(componentDir))
        return result
    }

    fun init(){
        this.files = findConfigurations()
        this.logFile = File(this.logDir, "${this.name.toLowerCase(Locale.getDefault())}.log")
        this.tfLog = File(this.logDir, "${this.name.toLowerCase(Locale.getDefault())}.log")
        this.workingDirectory = File(terraformSourceDir, "components/$component")
        this.runner = HookRunner()
        logFile.mkdirs()
        logFile.delete()
    }

    fun prepareCommand(action:String): List<String>{
        val command = ArrayList<String>()
        command.add(this.terraformExecutable?.absolutePath ?: "terraform")
        command.add(action)
        command.addAll(DEFAULT_TF_ARGS)
        command.addAll(this.files!!.map { f->  "-var-file=${f.absolutePath}" })
        return command
    }

    fun executeHook(suffix:String): Int {
        val componentDir = File(terraformSourceDir, "components/$component")
        val hooksDirectory = File(componentDir, "hooks")
        if(!hooksDirectory.exists() || !hooksDirectory.isDirectory){
            return Integer.MAX_VALUE
        }
        val scriptFile = hooksDirectory.listFiles { _, name ->
            name.startsWith("${this.name}-$suffix.")
        }.firstOrNull()
        if(scriptFile != null) {
            return this.runner.execute(scriptFile, workingDirectory, File(this.logDir, "$component-${scriptFile.name}.log"))
        }
        return Integer.MAX_VALUE
    }

    private fun scanDirectory(componentDir: File) :List<File> {
        if (!componentDir.exists() || !componentDir.isDirectory) {
            throw RuntimeException("${componentDir.absolutePath} isn't a directory")
        }
        return findConfiguration(componentDir, version)
    }

    private fun findConfiguration(sourceDir: File, suffix: String): List<File> {
        val result: ArrayList<File> = ArrayList()
        var files = File(sourceDir, ENVIRONMENTS_DIR)
                .listFiles { _, name -> name == "$environment.tfvars" }
        ofNullable(files).ifPresent { result.addAll(files) }
        files = File(sourceDir, ENVIRONMENTS_DIR)
                .listFiles { _, name -> name == "$environment-$suffix.tfvars" }
        ofNullable(files).ifPresent { result.addAll(files) }
        return result
    }

    @TaskAction
    fun apply(){
        init()
        val log = logFile.bufferedWriter(Charsets.UTF_8)
        log.use {
            log.write("Using variable files:\n ${files?.joinToString("") { f-> "\t${f.absolutePath}\n" }}\n")
            val command = prepareCommand(action)
            log.write("Working Directory: ${workingDirectory.absolutePath}\n")
            var result = this.executeHook("before")
            if(result != Integer.MAX_VALUE) log.write("Executed ${this.name}-before with return code $result\n")
            log.write("Command:\n\t${command.joinToString(" ")}\n")
            result = this.executeHook("after")
            if(result != Integer.MAX_VALUE) log.write("Executed ${this.name}-after with return code $result\n")
            val componentDir = File(terraformSourceDir, "components/$component")
            runner.executeCommand(command, componentDir, File(logDir, "component-${action}.log"))
            this.executeHook("after")
        }
    }

}