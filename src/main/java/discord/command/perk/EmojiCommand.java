package discord.command.perk;

import com.vdurmont.emoji.EmojiManager;
import discord.util.BotUtils;
import discord.data.UserManager;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.data.object.user.Name;
import discord.util.MessageUtils;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.TextChannel;

public class EmojiCommand extends AbstractCommand {

    public static final int LEVEL_REQUIRED = 60;

    public EmojiCommand() {
        super(new String[]{"emoji"}, 1, CommandCategory.PERK);
    }

    @Override
    public void execute(Message message, TextChannel channel, String[] args) {
        Name name = UserManager.getDUserFromMessage(message).getName();
        String emoji = args[0];

        //EmojiManager makes it easy to check for emoji
        if (EmojiManager.isEmoji(emoji)) {
            //some emojis take up 2 characters
            if (emoji.length() == 2) {
                name.setEmoji(Character.toCodePoint(emoji.charAt(0), emoji.charAt(1)));
            } else {
                name.setEmoji(emoji.codePointAt(0));
            }
            MessageUtils.sendInfoMessage(channel, "Splendid choice. Updated your name emoji to " + emoji);
            UserManager.saveDatabase();
        } else if (emoji.toLowerCase().equals("none")) {
            name.setEmoji(0);
            MessageUtils.sendInfoMessage(channel, "Your name emoji has been removed.");
            UserManager.saveDatabase();
        } else {
            MessageUtils.sendErrorMessage(channel, "Could not parse a unicode emoji from input.");
        }
    }

    @Override
    public int getLevelRequired() {
        return LEVEL_REQUIRED;
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "[emoji]", "Set an emoji next to your name."
                + "\n\nProviding `\"none\"` as the parameter instead will remove your current emoji.");
    }

}
