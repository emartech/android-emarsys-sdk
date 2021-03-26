package com.emarsys.testUtil.rules

import android.app.Application
import com.emarsys.testUtil.ConnectionTestUtils
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class ConnectionRule(private val application: Application) : TestRule {

    override fun apply(base: Statement?, description: Description?): Statement {
        return object : Statement() {
            override fun evaluate() {
                ConnectionTestUtils.checkConnection(application)
                base?.evaluate()
                return
            }
        };
    }


}