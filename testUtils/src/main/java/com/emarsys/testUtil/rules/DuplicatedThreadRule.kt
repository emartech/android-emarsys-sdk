package com.emarsys.testUtil.rules

import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class DuplicatedThreadRule(private val threadName: String) : TestRule {

    override fun apply(base: Statement?, description: Description?): Statement {
        return object : Statement() {
            override fun evaluate() {
                val threads = Thread.getAllStackTraces().keys.map { it.name }
                    .filter { it.startsWith(threadName) }
                if (threads.size > 1) {
                    throw Throwable("TEST: $threadName thread is duplicated")
                }
                base?.evaluate()
                return
            }
        }
    }


}