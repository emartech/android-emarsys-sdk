package com.emarsys.testUtil.rules

import android.util.Log
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement


class RetryRule(private val retryCount: Int) : TestRule {

    override fun apply(base: Statement, description: Description) = statement(base, description)

    private fun statement(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                var caughtThrowable: Throwable? = null

                for (i in 0..retryCount) {
                    try {
                        base.evaluate()
                        return
                    } catch (t: Throwable) {
                        caughtThrowable = t
                        Log.e("TEST", "${description.displayName}: run ${(i + 1)} failed.")
                    }
                }
                Log.e("TEST", "${description.displayName}: giving up after $retryCount failures.")
                throw caughtThrowable!!
            }
        }
    }
}