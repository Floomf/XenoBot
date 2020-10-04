package discord.command.utility;

import discord.util.BotUtils;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.util.MessageUtils;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;

public class RngCommand extends AbstractCommand {

    public RngCommand() {
        super(new String[]{"rng", "number", "num"}, 1, CommandCategory.UTILITY);
    }

    @Override
    public void execute(Message message, TextChannel channel, String[] args) {
        try {
            int limit = Integer.parseInt(args[0]);
            if (limit > 0) {
                MessageUtils.sendMessage(channel, "Result",
                        String.valueOf((int) (Math.random() * limit + 1)));
            }
        } catch (NumberFormatException e) {
            MessageUtils.sendErrorMessage(channel, "Please supply an integer greater than zero.");
        }
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "[max]",
                "Generate an number from 1 to the max.");
    }
}
