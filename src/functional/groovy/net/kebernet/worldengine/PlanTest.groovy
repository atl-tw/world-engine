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

class PlanTest extends Specification {
    @Rule
    TemporaryFolder testProjectDir = new TemporaryFolder()

    def mainDev = new File("build/functional/projects/simple/src/deploy/terraform/environments/dev.tfvars")
    def app1Dev = new File("build/functional/projects/simple/src/deploy/terraform/components/application1/environments/dev.tfvars")
    def appt1Dev1 = new File("build/functional/projects/simple/src/deploy/terraform/components/application1/environments/dev-1.tfvars")

    def setup() {
        def from = FileSystems.default.getPath(new File("src/functional/projects").getAbsolutePath())
        def to = FileSystems.default.getPath(new File("build/functional/projects").getAbsolutePath())
        Files.createDirectories(to)
        Files.walkFileTree(from, new CopyDir(from, to))

    }

    def "can find dev files "() {
        when:
        def result = GradleRunner.create()
                .withProjectDir(new File("build/functional/projects/simple"))
                .withArguments('plan',
                "-Pwe.terraformExecutable=${new File("src/functional/bin/fake-terraform").getAbsolutePath()}")
                .withPluginClasspath()
                .withDebug(true)
                .forwardStdOutput(new OutputStreamWriter(System.out))
                .build()
        then:
        result.task(":plan").outcome == SUCCESS
        def file = new File("build/functional/projects/simple/build/world-engine/plan.log").getText("UTF-8")
        file.contains("\t${mainDev.absolutePath}")
        file.contains("\t${app1Dev.absolutePath}")
    }

    def "can find version files "() {
        when:
        def result = GradleRunner.create()
                .withProjectDir(new File("build/functional/projects/simple"))
                .withArguments('plan', "-Pwe.version=1",
                "-Pwe.terraformExecutable=${new File("src/functional/bin/fake-terraform").getAbsolutePath()}")
                .withPluginClasspath()
                .withDebug(true)
                .forwardStdOutput(new OutputStreamWriter(System.out))
                .build()
        then:
        result.task(":plan").outcome == SUCCESS
        def file = new File("build/functional/projects/simple/build/world-engine/plan.log").getText("UTF-8")
        file.contains("\t${mainDev.absolutePath}")
        file.contains("\t${app1Dev.absolutePath}")
        file.contains("\t${appt1Dev1.absolutePath}")
    }

    def "can execute hooks "() {
        when:
        def result = GradleRunner.create()
                .withProjectDir(new File("build/functional/projects/hooks"))
                .withArguments('foo', "-Pwe.version=1",
                "-Pwe.terraformExecutable=${new File("src/functional/bin/fake-terraform").getAbsolutePath()}",
                "--stacktrace")
                .withPluginClasspath()
                .withDebug(true)
                .forwardStdOutput(new OutputStreamWriter(System.out))
                .build()

        then:
        result.task(":foo").outcome == SUCCESS
        def file = new File("build/functional/projects/hooks/build/world-engine/foo.log").getText("UTF-8")
        file.contains("Executed foo-before with return code 0")
        file.contains("Executed foo-after with return code 0")
        def before = new File("build/functional/projects/hooks/src/deploy/terraform/components/application1/before.txt").getText("UTF-8")
        before == "BEFORE!\n"
        def after = new File("build/functional/projects/hooks/src/deploy/terraform/components/application1/after.txt").getText("UTF-8")
        after == 'AFTER! Hello!\n'

    }
}