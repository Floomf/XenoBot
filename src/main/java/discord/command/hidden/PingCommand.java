package discord.command.hidden;

import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.util.BotUtils;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class PingCommand extends AbstractCommand {

    public PingCommand() {
        super("ping", 0, CommandCategory.HIDDEN);
    }

    @Override
    public void execute(Message message, TextChannel channel, String[] args) {
        Instant time = message.getTimestamp();
        Message pongMessage = channel.createMessage("Pong!").block();
        pongMessage.edit(spec -> spec.setContent(time.until(pongMessage.getTimestamp(), ChronoUnit.MILLIS) + "ms")).block();
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "", "Ping.");
    }

}
