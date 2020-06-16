package com.emarsys.mobileengage.notification.command

class CompositeCommand(val commands: List<Runnable>) : Runnable {

    override fun run() {
        val filterIsInstance = commands.filterIsInstance<LaunchApplicationCommand>()
        filterIsInstance.forEach {
            it.run()
        }
        (commands - filterIsInstance).forEach {
            it.run()
        }
    }
}