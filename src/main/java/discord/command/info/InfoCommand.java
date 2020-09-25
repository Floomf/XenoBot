package discord.command.info;

import discord.core.command.CommandManager;
import discord.data.UserManager;
import discord.util.BotUtils;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;


import discord.util.DiscordColor;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;

public class InfoCommand extends AbstractCommand {

    private static final long START_TIME = System.currentTimeMillis();

    public InfoCommand() {
        super(new String[]{"info", "stats", "uptime", "xeno"}, 0, CommandCategory.INFO);
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
    public void execute(Message message, TextChannel channel, String[] args) {
        channel.createEmbed(embed -> {
            embed.setAuthor(message.getClient().getSelf().block().getUsername(), "", message.getClient().getSelf().block().getAvatarUrl());
            embed.setThumbnail(message.getClient().getSelf().block().getAvatarUrl());
            embed.setColor(DiscordColor.CYAN);

            embed.addField("Version 🏷", BotUtils.getVersion(), true);
            embed.addField("Uptime 🕓", formatElapsedTime(System.currentTimeMillis() - START_TIME), true);
            embed.addField("Guilds 🏘", String.valueOf(message.getClient().getGuilds().collectList().block().size()), true);
            embed.addField("Users 🚹", String.valueOf(UserManager.getDUsers().size()), true);
            embed.addField("Commands ⌨", String.valueOf(CommandManager.getAllCommands().size()), true);
        }).block();
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "", "View various stats about Xeno.");
    }

}
