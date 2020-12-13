package discord.command.admin;

import discord.data.object.Poll;
import discord.util.BotUtils;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;

import discord.util.MessageUtils;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.entity.Message;
import discord4j.rest.util.Color;

import java.util.Arrays;

public class PollCommand extends AbstractCommand {

    public PollCommand() {
        super(new String[]{"poll"}, 4, CommandCategory.ADMIN);
    }

    @Override
    public void execute(Message message, TextChannel channel, String[] args) {
        if (!args[0].toLowerCase().matches("[a-f\\d]{6}")) {
            MessageUtils.sendErrorMessage(channel, "Couldn't parse a valid hex color code.");
            return;
        }

        if (!args[1].matches("[1-9]\\d{0,9}")) {// 1-999,999,999
            MessageUtils.sendErrorMessage(channel, "Couldn't parse a valid amount of hours for the poll duration.");
            return;
        }

        TextChannel pollChannel = BotUtils.getGuildTextChannel("polls", channel.getGuild().block());
        new Poll(pollChannel, Color.of(Integer.parseInt(args[0], 16)), Integer.parseInt(args[1]), args[2], Arrays.copyOfRange(args, 3, args.length));
        MessageUtils.sendInfoMessage(channel, "Poll created in " + pollChannel.getMention() + " chat.");
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "[hex color] [hours] [title] [options]",
                "Create a custom poll.\n\nTo include multiple words in an argument, "
                        + "you must wrap it in quotations."
                        + "\n*Example:* `!poll FF0000 24 \"Candy corn?\" \"Yes, of course!\" No`");
    }

}
