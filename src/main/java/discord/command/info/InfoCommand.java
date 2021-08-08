package discord.command.info;

import discord.core.command.CommandManager;
import discord.core.command.InteractionContext;
import discord.util.BotUtils;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;

import discord.util.DiscordColor;
import discord4j.discordjson.json.ApplicationCommandRequest;

public class InfoCommand extends AbstractCommand {

    private static final long START_TIME = System.currentTimeMillis();

    public InfoCommand() {
        super("xeno", 0, CommandCategory.INFO);
    }

    @Override
    public ApplicationCommandRequest buildSlashCommand() {
        return ApplicationCommandRequest.builder()
                .name(getName())
                .description("View information about Xeno")
                .build();
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
    public void execute(InteractionContext context) {
        context.reply(embed -> {
            embed.setAuthor(context.getChannel().getClient().getSelf().block().getUsername(), "",
                    context.getChannel().getClient().getSelf().block().getAvatarUrl());
            embed.setThumbnail(context.getChannel().getClient().getSelf().block().getAvatarUrl());
            embed.setColor(DiscordColor.CYAN);
            embed.setDescription("*Hi there!*");

            embed.addField("Developer 👨‍💻", "<@98235887866908672>", true);
            embed.addField("Version 🏷", BotUtils.getVersion(), true);
            embed.addField("Uptime 🕓", formatElapsedTime(System.currentTimeMillis() - START_TIME), true);
            embed.addField("Guilds 🏘", String.valueOf(context.getChannel().getClient().getGuilds().collectList().block().size()), true);
            embed.addField("Commands ⌨", CommandManager.getAllCommands().size()
                    + " (" + CommandManager.getGlobalCommandsCount() + " global)", true);
        });
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "", "View various stats about Xeno.");
    }

    @Override
    public boolean isSupportedGlobally() {
        return true;
    }

}
