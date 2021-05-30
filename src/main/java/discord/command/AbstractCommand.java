package discord.command;

import discord.core.command.InteractionContext;
import discord4j.core.event.domain.InteractionCreateEvent;
import discord4j.core.object.command.ApplicationCommandInteraction;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.discordjson.json.ApplicationCommandRequest;

public abstract class AbstractCommand {

    private final String[] names;
    private final int argsRequired;
    private final CommandCategory category;

    public AbstractCommand(String[] names, int argsNeeded, CommandCategory category) {
        this.names = names;
        this.argsRequired = argsNeeded;
        this.category = category;
    }

    public ApplicationCommandRequest buildSlashCommand() {
        return null;
    }

    public ApplicationCommandRequest buildOutsideGuildSlashCommand() {
        return null;
    }

    //handle through slash command
    public void execute(InteractionContext context) {

    }

    //handle through a channel message (old way)
    public void execute(Message message, TextChannel channel, String[] args) {

    }

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
