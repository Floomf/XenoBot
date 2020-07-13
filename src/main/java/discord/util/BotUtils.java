package discord.util;

import discord.core.command.CommandManager;
import discord.Main;

import java.io.IOException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.TextChannel;

public class BotUtils {

    public static TextChannel getGuildTextChannel(String name, Guild guild) {
        return guild.getChannels().ofType(TextChannel.class)
                .filter(channel -> channel.getName().equals(name))
                .collectList().block().get(0); //TODO this will blow up if channel doesn't exist
    }

    public static Role getGuildRole(String name, Guild guild) {
        return guild.getRoles().filter(role -> role.getName().equalsIgnoreCase(name)).collectList().block().get(0);
    }

    public static String getGuildEmojiString(Guild guild, String emojiName) {
        return guild.getEmojis().filter(e -> e.getName().equals(emojiName)).collectList().block().get(0).asFormat(); //will break if emoji doesnt exist
    }

    public static String getRandomGuildEmoji(Guild guild, String[] emojiNames) {
        return getGuildEmojiString(guild, emojiNames[(int) (Math.random() * emojiNames.length)]);
    }

    public static String buildUsage(String alias, String args, String desc) {
        return (String.format("`%s%s%s` \n\n%s", CommandManager.CMD_PREFIX, alias, args.isEmpty() ? "" : " " + args, desc));
    }

    //Takes a string and strips it of any non-basic characters and symbols
    public static String validateString(String string) {
        Matcher m = Pattern.compile("([\u0020-\u00FF]+)").matcher(string);
        String result = "";
        while (m.find()) {
            result += m.group(1);
        }
        return result;
    }

    public static String validateNick(String nick) {
        String result = validateString(nick);
        if (result.length() > 30) { //cant have nick too long to conflict with prestige symbol
            result = result.substring(0, 30);
        }
        return result.trim(); //trim in case the string is cut off and the last char is a space
    }

    public static String getVersion() {
        try {
            final Properties properties = new Properties();
            properties.load(Main.class.getClassLoader().getResourceAsStream("project.properties"));
            return properties.getProperty("version");
        } catch (IOException e) {
            return "Version could not be retrieved with error " + e;
        }
    }


}
