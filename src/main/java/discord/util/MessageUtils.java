package discord.util;

import discord.Main;
import discord4j.core.object.entity.PrivateChannel;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.spec.EmbedCreateSpec;

import java.awt.*;
import java.util.function.Consumer;

public class MessageUtils {

    public static Consumer<EmbedCreateSpec> message(String title, String desc, Color color) {
        return embed -> {
            embed.setAuthor(title, "", Main.BOT_AVATAR_URL);
            embed.setDescription(desc);
            embed.setColor(color);
        };
    }

    public static Consumer<EmbedCreateSpec> message(String title, String desc) {
        return message(title, desc, Color.WHITE);
    }

    public static void sendMessage(PrivateChannel channel, String title, String desc, Color color) {
        channel.createMessage(spec -> spec.setEmbed(message(title, desc, color))).block();
    }

    public static void sendMessage(TextChannel channel, String title, String desc, Color color) {
        channel.createMessage(spec -> spec.setEmbed(message(title, desc, color))).block();
    }

    public static void sendMessage(TextChannel channel, String title, String desc) {
        sendMessage(channel, title, desc, Color.WHITE);
    }

    public static void sendInfoMessage(TextChannel channel, String message) {
        sendMessage(channel, "Info", message, Color.CYAN);
    }

    public static void sendErrorMessage(TextChannel channel, String message) {
        sendMessage(channel, "Error", message, Color.RED);
    }

    public static void sendUsageMessage(TextChannel channel, String message) {
        sendMessage(channel, "Usage", message, Color.ORANGE);
    }

}
