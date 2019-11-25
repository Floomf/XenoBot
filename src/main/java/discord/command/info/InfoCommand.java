package discord.command.info;

import discord.core.command.CommandManager;
import discord.data.UserManager;
import discord.util.BotUtils;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;

import java.awt.Color;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.TextChannel;

public class InfoCommand extends AbstractCommand {

    private static final long START_TIME = System.currentTimeMillis();

    public InfoCommand() {
        super(new String[]{"info", "stats", "uptime"}, 0, CommandCategory.INFO);
    }

    @Override
    public void execute(Message message, TextChannel channel, String[] args) {
        channel.createMessage(spec -> spec.setEmbed(embed -> {
            embed.setAuthor(message.getClient().getSelf().block().getUsername(), "", "");
            embed.setThumbnail(message.getClient().getSelf().block().getAvatarUrl());
            embed.setColor(Color.CYAN);

            embed.addField("Version 🏷", "`" + BotUtils.getVersion() + "`", true);
            embed.addField("Uptime 🕓", "`" + formatElapsedTime(System.currentTimeMillis() - START_TIME) + "`", true);
            embed.addField("Guilds 🏘", "`" + message.getClient().getGuilds().collectList().block().size() + "`", true);
            embed.addField("Users 🚹", "`" + UserManager.size() + "`", true);
            embed.addField("Commands ⌨", "`" + CommandManager.getAllCommands().size() + "`", true);
        })).block();
    }

    private static String formatElapsedTime(long millis) {
        String result = "";
        long totalSeconds = millis / 1000;

        //All of these are about getting the remainder units of time
        long s = totalSeconds % 60;
        long m = (totalSeconds / 60) % 60;
        long h = (totalSeconds / 60 / 60) % 24;
        long d = (totalSeconds / 60 / 60 / 24);

        if (d > 0) {
            result += d + "d ";
        }
        if (h > 0) {
            result += h + "h ";
        }
        if (m > 0) {
            result += m + "m ";
        }
        if (s > 0) {
            result += s + "s ";
        }

        return result.trim(); //Way to get rid of last space
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "", "View various stats about Xeno.");
    }

}
