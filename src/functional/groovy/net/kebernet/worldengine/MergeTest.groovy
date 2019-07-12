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

import com.grack.nanojson.JsonParser
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification

import java.nio.file.FileSystems
import java.nio.file.Files

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class MergeTest  extends Specification {

    def setup() {
        def from = FileSystems.default.getPath(new File("src/functional/projects").getAbsolutePath())
        def to = FileSystems.default.getPath(new File("build/functional/projects").getAbsolutePath())
        Files.createDirectories(to)
        Files.walkFileTree(from, new CopyDir(from, to))

    }
    def "can find merge files "() {
        def out = new File("build/functional/projects/versions/build/world-engine/versions.json")
        if(out.exists()) out.delete()
        when:
        def result = GradleRunner.create()
                .withProjectDir(new File("build/functional/projects/versions"))
                .withArguments('version')
                .withPluginClasspath()
                .withDebug(true)
                .forwardStdOutput(new OutputStreamWriter(System.out))
                .build()
        then:
        result.task(":version").outcome == SUCCESS
        def file = out.getText("UTF-8")
        def read = JsonParser.object().from(file)
        read.get("foo") == "4"
        read.get("bar") == "2"
        read.get("baz") == "1"
    }
}
