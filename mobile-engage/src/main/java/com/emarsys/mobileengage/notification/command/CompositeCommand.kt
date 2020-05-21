package com.emarsys.mobileengage.notification.command

class CompositeCommand(val commands: List<Runnable>) : Runnable {

    override fun run() {
        for (command in commands) {
            command.run()
        }
    }
}