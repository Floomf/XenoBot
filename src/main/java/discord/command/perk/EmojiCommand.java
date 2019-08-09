package discord.command.perk;

import com.vdurmont.emoji.EmojiManager;
import discord.util.BotUtils;
import discord.data.UserManager;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.data.object.Name;
import sx.blah.discord.handle.obj.IMessage;

public class EmojiCommand extends AbstractCommand{
    
    public static final int LEVEL_REQUIRED = 60; 
    
    public EmojiCommand() {
        super(new String[] {"emoji"}, 1, LEVEL_REQUIRED, CommandCategory.PERK); 
    }
    
    @Override
    public void execute(IMessage message, String[] args) {
        Name name = UserManager.getDBUserFromMessage(message).getName();
        String emoji = args[0];
        
        //EmojiManager makes it easy to check for emoji
        if (EmojiManager.isEmoji(emoji)) {
            //some emojis take up 2 characters
            if (emoji.length() == 2) {
                name.setEmoji(Character.toCodePoint(emoji.charAt(0), emoji.charAt(1)), message.getGuild());
            } else {
                name.setEmoji(emoji.codePointAt(0), message.getGuild());
            }
            BotUtils.sendInfoMessage(message.getChannel(), "Splendid choice. Updated your name emoji to " + emoji);
            UserManager.saveDatabase();
        } else if (emoji.toLowerCase().equals("none")) {
            name.setEmoji(0, message.getGuild());
            BotUtils.sendInfoMessage(message.getChannel(), "Your name emoji has been removed.");
            UserManager.saveDatabase();
        } else {
            BotUtils.sendErrorMessage(message.getChannel(), "Could not parse a unicode emoji from input.");
        }
    }
    
    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "[emoji]", "Set an emoji next to your name."
                + "\n\nProviding `\"none\"` as the parameter instead will remove your current emoji.");
    }
    
}
