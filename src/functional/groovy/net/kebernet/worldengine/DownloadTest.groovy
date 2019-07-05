package net.kebernet.worldengine

import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import java.nio.file.FileSystems
import java.nio.file.Files

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class DownloadTest extends Specification {
    @Rule
    TemporaryFolder testProjectDir = new TemporaryFolder()


    def setup() {
        def from = FileSystems.default.getPath(new File("src/functional/projects").getAbsolutePath())
        def to = FileSystems.default.getPath(new File("build/functional/projects").getAbsolutePath())
        Files.createDirectories(to)
        Files.walkFileTree(from, new CopyDir(from, to))


    }

    def "downloads stock file"() {
        when:
        new File("build/functional/projects/download/.gradle/terraform").deleteDir()
        def result = GradleRunner.create()
                .withProjectDir(new File("build/functional/projects/download"))
                .withArguments('download')
                .withPluginClasspath()
                .withDebug(true)
                .forwardStdOutput(new OutputStreamWriter(System.out))
                .build()
        then:
        result.task(":download").outcome == SUCCESS

    }

    def "downloads doesn't download when present"() {
        when:
        def result = GradleRunner.create()
                .withProjectDir(new File("build/functional/projects/download"))
                .withArguments('download')
                .withPluginClasspath()
                .withDebug(true)
                .forwardStdOutput(new OutputStreamWriter(System.out))
                .build()
        then:
        result.task(":download").outcome == SUCCESS

    }


}