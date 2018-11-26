package discord.command.perk;

import com.vdurmont.emoji.EmojiManager;
import discord.BotUtils;
import discord.UserManager;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.object.Name;
import discord.object.User;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;

public class EmojiCommand extends AbstractCommand{
    
    public EmojiCommand() {
        super(new String[] {"emoji"}, 1, CommandCategory.PERK); 
    }
    
    public void execute(IMessage message, String[] args) {
        User user = UserManager.getDBUserFromMessage(message);
        IChannel channel = message.getChannel();
        String emoji = args[0];
        
        if (!(user.getProgress().getTotalLevel() >= 40)) {
            BotUtils.sendErrorMessage(channel, "You must be at least level **40** to set your name emoji!"
                    + " You can view your progress with `!prog`.");
            return;
        }
        
        Name name = user.getName();
        //EmojiManager makes it easy to check for emoji
        if (EmojiManager.isEmoji(emoji)) {
            //some emojis take up 2 characters
            if (emoji.length() == 2) {
                name.setEmoji(Character.toCodePoint(emoji.charAt(0), emoji.charAt(1)), message.getGuild());
            } else {
                name.setEmoji(emoji.codePointAt(0), message.getGuild());
            }
            BotUtils.sendInfoMessage(channel, "Splendid choice. Updated your name emoji to " + emoji);
            UserManager.saveDatabase();
        } else if (emoji.toLowerCase().equals("none")) {
            name.setEmoji(0, message.getGuild());
            BotUtils.sendInfoMessage(channel, "Your name emoji has been removed.");
            UserManager.saveDatabase();
        } else {
            BotUtils.sendErrorMessage(channel, "Could not parse a unicode emoji from input.");
        }      
    }
    
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "[emoji]", "Set an emoji next to your name."
                + " Providing `\"none\"` as the parameter will remove your current emoji."            
                + "\n*(Level 40+ or Prestiged)*");
    }
    
}
