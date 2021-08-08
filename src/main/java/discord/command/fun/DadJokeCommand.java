package discord.command.fun;

import discord.core.command.InteractionContext;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.discordjson.json.ApplicationCommandRequest;
import kong.unirest.Unirest;
import discord.util.BotUtils;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.util.MessageUtils;
import discord4j.core.object.entity.Message;

public class DadJokeCommand extends AbstractCommand {

    public DadJokeCommand() {
        super("dadjoke", 0, CommandCategory.FUN);
    }

    @Override
    public ApplicationCommandRequest buildSlashCommand() {
        return ApplicationCommandRequest.builder()
                .name(getName())
                .description("Read a random dad joke")
                .build();
    }

    @Override
    public void execute(InteractionContext context) {
        context.reply(embed -> embed.setDescription("**" +
                Unirest.get("https://icanhazdadjoke.com/")
                        .header("Accept", "text/plain")
                        .header("User-Agent", "Discord Bot")
                        .asString().getBody() + "**"));
    }

    @Override
    public void execute(Message message, TextChannel channel, String[] args) {
        MessageUtils.sendMessage(channel, "Get this:", String.format("**%s**",
                Unirest.get("https://icanhazdadjoke.com/")
                        .header("Accept", "text/plain")
                        .header("User-Agent", "Discord Bot")
                        .asString().getBody()));
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "", "View a random dad joke.");
    }

    @Override
    public boolean isSupportedGlobally() {
        return true;
    }

}
