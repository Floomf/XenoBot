package discord.util;

import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.spec.legacy.LegacyEmbedCreateSpec;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

public class MessageUtils {

    public static Consumer<LegacyEmbedCreateSpec> getEmbed(String title, String desc, Color color) {
        return embed -> {
            embed.setAuthor(title, "", BotUtils.BOT_AVATAR_URL);
            embed.setDescription(desc);
            embed.setColor(color);
        };
    }

    public static Consumer<LegacyEmbedCreateSpec> getEmbed(String title, String desc) {
        return getEmbed(title, desc, Color.DISCORD_WHITE);
    }

    public static Consumer<LegacyEmbedCreateSpec> getErrorEmbed(String message) {
        return getEmbed("Error", message, DiscordColor.RED);
    }

    public static Consumer<LegacyEmbedCreateSpec> getInfoEmbed(String message) {
        return getEmbed("Info", message, DiscordColor.GREEN);
    }

    public static void sendMessage(MessageChannel channel, Consumer<LegacyEmbedCreateSpec> embed) {
        channel.createMessage(spec -> {
            spec.addEmbed(embed);
        }).onErrorResume(e -> Mono.empty()).block();
    }

    public static void sendMessage(MessageChannel channel, String title, String desc, Color color) {
        sendMessage(channel, getEmbed(title, desc, color));
    }

    public static void sendMessage(TextChannel channel, String title, String desc) {
        sendMessage(channel, title, desc, Color.DISCORD_WHITE);
    }

    public static void sendInfoMessage(TextChannel channel, String message) {
        sendMessage(channel, getInfoEmbed(message));
    }

    public static void sendErrorMessage(TextChannel channel, String message) {
        sendMessage(channel, getErrorEmbed(message));
    }

    public static void sendUsageMessage(TextChannel channel, String message) {
        sendMessage(channel, "Usage", message, DiscordColor.YELLOW);
    }

}
