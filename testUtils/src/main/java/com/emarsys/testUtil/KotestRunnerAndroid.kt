package com.emarsys.testUtil

import io.kotest.core.config.EmptyExtensionRegistry
import io.kotest.core.config.ProjectConfiguration
import io.kotest.core.spec.Spec
import io.kotest.engine.TestEngineLauncher
import io.kotest.engine.spec.Materializer
import io.kotest.engine.spec.createAndInitializeSpec
import io.kotest.engine.test.names.DefaultDisplayNameFormatter
import kotlinx.coroutines.runBlocking
import org.junit.runner.Description
import org.junit.runner.Runner
import org.junit.runner.notification.RunNotifier

class KotestRunnerAndroid(
    private val kClass: Class<out Spec>
) : Runner() {
    private val formatter = DefaultDisplayNameFormatter(ProjectConfiguration())

    override fun run(notifier: RunNotifier) {
        runBlocking {
            val listener = JUnitTestEngineListener(notifier)
            TestEngineLauncher(listener).withClasses(kClass.kotlin).launch()
        }
    }

    override fun getDescription(): Description {
        val spec = runBlocking {
            createAndInitializeSpec(
                kClass.kotlin,
                EmptyExtensionRegistry
            ).getOrThrow()
        }
        val desc = Description.createSuiteDescription(spec::class.java)
        Materializer(ProjectConfiguration()).materialize(spec).forEach { rootTest ->
            desc.addChild(
                describeTestCase(
                    rootTest,
                    formatter.format(rootTest)
                )
            )
        }
        return desc
    }
}