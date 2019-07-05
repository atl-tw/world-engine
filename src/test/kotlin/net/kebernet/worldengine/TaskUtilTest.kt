package net.kebernet.worldengine

import org.gradle.api.logging.Logger
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import java.io.File

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


}