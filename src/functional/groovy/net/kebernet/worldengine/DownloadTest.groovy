/*
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

    def "downloads old file"() {
        when:
        new File("build/functional/projects/download-old-version/.gradle/terraform").deleteDir()
        def result = GradleRunner.create()
                .withProjectDir(new File("build/functional/projects/download-old-version"))
                .withArguments('download')
                .withPluginClasspath()
                .withDebug(true)
                .forwardStdOutput(new OutputStreamWriter(System.out))
                .build()
        then:
        result.task(":download").outcome == SUCCESS
        new File("build/functional/projects/download-old-version/.gradle/terraform", "terraform").exists()

    }



    def "downloads bsd file"() {
        when:
        new File("build/functional/projects/download-bsd-version/.gradle/terraform").deleteDir()
        def result = GradleRunner.create()
                .withProjectDir(new File("build/functional/projects/download-bsd-version"))
                .withArguments('download')
                .withPluginClasspath()
                .withDebug(true)
                .forwardStdOutput(new OutputStreamWriter(System.out))
                .build()
        then:
        result.task(":download").outcome == SUCCESS
        new File("build/functional/projects/download-bsd-version/.gradle/terraform", "terraform").exists()

    }
}