package discord.command.perk;

import com.vdurmont.emoji.EmojiManager;
import com.vdurmont.emoji.EmojiParser;
import discord.core.command.CommandHandler;
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
        super(new String[]{"emoji", "emojis"}, 1, CommandCategory.PERK);
    }

    @Override
    public void execute(Message message, TextChannel channel, String[] args) {
        Name name = UserManager.getDUserFromMessage(message).getName();
        String emojis = CommandHandler.combineArgs(0, args).replace(" ", "");
        //EmojiManager makes it easy to check for emoji
        if (EmojiManager.isOnlyEmojis(emojis)) {
            name.setEmojis(EmojiParser.extractEmojis(emojis).stream().limit(3).toArray(String[]::new));
            MessageUtils.sendInfoMessage(channel, "Splendid choice. Updated your name emoji(s) accordingly.");
            UserManager.saveDatabase();
        } else if (emojis.toLowerCase().equals("none")) {
            name.setEmojis(new String[0]);
            MessageUtils.sendInfoMessage(channel, "Your name emojis have been removed.");
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
        return BotUtils.buildUsage(alias, "[emoji(s)]", "Set up to three emojis next to your name."
                + "\n\nProviding `none` as the parameter instead will remove your current emojis.");
    }

}
