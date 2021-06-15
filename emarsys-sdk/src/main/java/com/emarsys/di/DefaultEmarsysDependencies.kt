package com.emarsys.di

import com.emarsys.config.EmarsysConfig

open class DefaultEmarsysDependencies(config: EmarsysConfig,
                                      testComponent: DefaultEmarsysComponent? = null) {

    private val component: DefaultEmarsysComponent = testComponent
            ?: DefaultEmarsysComponent(config)

    init {
        setupEmarsysComponent(component)

        emarsys().coreSdkHandler.post {
            component.initializeResponseHandlers(config)
        }
    }
}