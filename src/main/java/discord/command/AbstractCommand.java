package discord.command;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;

public abstract class AbstractCommand {

    private final String[] names;
    private final int argsRequired;
    private final CommandCategory category;

    public AbstractCommand(String[] names, int argsNeeded, CommandCategory category) {
        this.names = names;
        this.argsRequired = argsNeeded;
        this.category = category;
    }

    public abstract void execute(Message message, TextChannel channel, String[] args);

    public abstract String getUsage(String alias);

    public String getName() {
        return names[0];
    }

    public String[] getNames() {
        return names;
    }

    public int getArgsRequired() {
        return argsRequired;
    }

    public int getLevelRequired() {
        return 0;
    }

    public CommandCategory getCategory() {
        return category;
    }

    public boolean isSupportedGlobally() {
        return false;
    }
}
