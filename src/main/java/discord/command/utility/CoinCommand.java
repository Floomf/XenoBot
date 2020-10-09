package discord.command.utility;

import discord.util.BotUtils;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.util.MessageUtils;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;

public class CoinCommand extends AbstractCommand {

    public CoinCommand() {
        super(new String[]{"flip", "toss", "coin"}, 0, CommandCategory.UTILITY);
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
