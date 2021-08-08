package discord.command.utility;

import discord.core.command.InteractionContext;
import discord.util.BotUtils;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.util.MessageUtils;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.discordjson.json.ApplicationCommandRequest;

public class CoinCommand extends AbstractCommand {

    public CoinCommand() {
        super("flip", 0, CommandCategory.UTILITY);
    }

    @Override
    public ApplicationCommandRequest buildSlashCommand() {
        return ApplicationCommandRequest.builder()
                .name(getName())
                .description("Flip a coin")
                .build();
    }

    @Override
    public void execute(InteractionContext context) {
        if (Math.random() < 0.5) {
            context.reply(MessageUtils.getEmbed("Heads!", "").andThen(embed -> embed.setImage("https://i.imgur.com/elztDZ2.png")));
        } else {
            context.reply(MessageUtils.getEmbed("Tails!", "").andThen(embed -> embed.setImage("https://i.imgur.com/fSsS54o.png")));
        }
    }


    @Override
    public void execute(Message message, TextChannel channel, String[] args) {
        String result = "Heads";
        if (Math.random() < 0.5) {
            result = "Tails";
        }

        MessageUtils.sendMessage(channel, "Result", result);
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "", "Flip a coin.");
    }

    @Override
    public boolean isSupportedGlobally() {
        return true;
    }

}
