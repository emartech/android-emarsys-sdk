package com.emarsys.mobileengage.notification.command;

import com.emarsys.core.util.Assert;

import java.util.List;

public class CompositeCommand implements Runnable {

    private final List<Runnable> commands;

    public CompositeCommand(List<Runnable> commands) {
        Assert.notNull(commands, "Commands must not be null!");
        Assert.elementsNotNull(commands, "Command elements must not be null!");
        this.commands = commands;
    }

    @Override
    public void run() {
        for (Runnable command : commands) {
            command.run();
        }
    }

    public List<Runnable> getCommands() {
        return commands;
    }
}
