package com.emarsys.di

import com.emarsys.config.EmarsysConfig
import com.emarsys.core.storage.EncryptedSharedPreferencesToSharedPreferencesMigration

open class DefaultEmarsysDependencies(config: EmarsysConfig,
                                      testComponent: DefaultEmarsysComponent? = null) {

    private val component: DefaultEmarsysComponent = testComponent
            ?: DefaultEmarsysComponent(config)

    init {
        setupEmarsysComponent(component)

        emarsys().concurrentHandlerHolder.coreHandler.post {
            EncryptedSharedPreferencesToSharedPreferencesMigration().migrate(
                component.sharedPreferences,
                component.sharedPreferencesV3
            )
        }

        emarsys().concurrentHandlerHolder.coreHandler.post {
            component.initializeResponseHandlers(config)
        }
    }
}
