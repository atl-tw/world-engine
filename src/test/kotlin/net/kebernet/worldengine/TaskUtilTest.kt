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

import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonParser
import org.gradle.api.logging.Logger
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import java.io.File
import java.io.FileInputStream

internal class TaskUtilTest {

    @Test
    fun logFileThrowsOnError() {
        val logger = mock(Logger::class.java)
        var didThrow = false
        try {
            doLogFile(File("src/test/resources/terraform-fail.txt"),
                    logger = logger, logTerraformOutput = false,
                    failOnTerraformErrors = true, action = "plan")
        } catch (e: RuntimeException) {
            assertTrue(e.message?.contains("Error: Error applying plan:")!!)
            didThrow = true
        }
        assertTrue(didThrow)
    }

    @Test
    fun logFileOutput() {
        val logger = mock(Logger::class.java)
        doLogFile(File("src/test/resources/whatever.txt"),
                logger = logger, logTerraformOutput = true,
                failOnTerraformErrors = true, action = "plan")
        verify(logger).lifecycle(eq("Terraform[plan]: Whatever"))
    }

    @Test
    fun testScanDir() {
        var result = findConfiguration(File("./src/test/resources"), "foo", "bar")
        assertEquals(2, result.size)
        assert(result.contains(File("./src/test/resources/environments/foo.tfvars")))
        assert(result.contains(File("./src/test/resources/environments/foo-bar.tfvars")))

        result = findConfiguration(File("./src/test/resources"), "foo", "baz")
        assertEquals(1, result.size)
        assert(result.contains(File("./src/test/resources/environments/foo.tfvars")))
    }

    @Test
    fun updateVersionsNewTest() {
        val apply = """
            {
                "foo": "123"
            }
        """.trimIndent()
        val current = """
            {
                "bar": "456",
                "baz": "789"
            }
        """.trimIndent()
        val output = File("./build/test-data/newversion.json")
        updateVersions(current, apply, output)
        FileInputStream(output).use {
            fis ->
            val result: JsonObject = JsonParser.`object`().from(fis)
            assertEquals("456", result["bar"])
            assertEquals("789", result["baz"])
            assertEquals("123", result["foo"])
        }
    }

    @Test
    fun updateVersionsUpdateTest() {
        val apply = """
            {
                "baz": "790",
                "bar": "123"
                
            }
        """.trimIndent()
        val current = """
            {
                "bar": "456",
                "foo": "456",
                "baz": "790"
            }
        """.trimIndent()
        val output = File("./build/test-data/updateverrsion.json")
        updateVersions(current, apply, output)
        FileInputStream(output).use {
            fis ->
            val result: JsonObject = JsonParser.`object`().from(fis)
            assertEquals("123", result["bar"])
            assertEquals("790", result["baz"])
            assertEquals("456", result["foo"])
        }
    }
}