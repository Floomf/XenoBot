package discord.util;

import discord.Main;
import discord4j.core.object.entity.PrivateChannel;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.spec.EmbedCreateSpec;

import java.awt.*;
import java.util.function.Consumer;

public class MessageUtils {

    public static Consumer<EmbedCreateSpec> getEmbed(String title, String desc, Color color) {
        return embed -> {
            embed.setAuthor(title, "", Main.BOT_AVATAR_URL);
            embed.setDescription(desc);
            embed.setColor(color);
        };
    }

    public static Consumer<EmbedCreateSpec> getEmbed(String title, String desc) {
        return getEmbed(title, desc, Color.WHITE);
    }

    public static void sendMessage(PrivateChannel channel, String title, String desc, Color color) {
        channel.createEmbed(getEmbed(title, desc, color)).block();
    }

    public static void sendMessage(TextChannel channel, String title, String desc, Color color) {
        channel.createEmbed(getEmbed(title, desc, color)).block();
    }

    public static void sendMessage(TextChannel channel, String title, String desc) {
        sendMessage(channel, title, desc, Color.WHITE);
    }

    public static void sendInfoMessage(TextChannel channel, String message) {
        sendMessage(channel, "Info", message, DiscordColor.CYAN.getColor());
    }

    public static void sendErrorMessage(TextChannel channel, String message) {
        sendMessage(channel, "Error", message, DiscordColor.RED.getColor());
    }

    public static void sendUsageMessage(TextChannel channel, String message) {
        sendMessage(channel, "Usage", message, DiscordColor.YELLOW.getColor());
    }

}
