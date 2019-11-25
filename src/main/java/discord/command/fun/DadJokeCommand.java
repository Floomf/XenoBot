package discord.command.fun;

import kong.unirest.Unirest;
import discord.util.BotUtils;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.util.MessageUtils;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.TextChannel;

public class DadJokeCommand extends AbstractCommand {

    public DadJokeCommand() {
        super(new String[]{"dadjoke", "dj"}, 0, CommandCategory.FUN);
    }

    @Override
    public void execute(Message message, TextChannel channel, String[] args) {
        MessageUtils.sendMessage(channel, "Get this:", String.format("`%s`",
                Unirest.get("https://icanhazdadjoke.com/")
                        .header("Accept", "text/plain")
                        .header("User-Agent", "Discord Bot")
                        .asString().getBody()));
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "", "View a random dad joke.");
    }

}
