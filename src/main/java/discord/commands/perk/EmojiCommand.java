package discord.commands.perk;

import com.vdurmont.emoji.EmojiManager;
import discord.BotUtils;
import discord.NameManager;
import discord.UserManager;
import discord.commands.AbstractCommand;
import discord.commands.CommandCategory;
import discord.objects.User;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;

public class EmojiCommand extends AbstractCommand{
    
    public EmojiCommand() {
        super(new String[] {"emoji"}, 1, CommandCategory.PERK); 
    }
    
    public void execute(IMessage message, String[] args) {
        User user = UserManager.getUserFromID(message.getAuthor().getLongID());
        IChannel channel = message.getChannel();
        String emoji = args[0];
        
        if (!(user.getLevel() >= 40 || user.getPrestige() > 0)) {
            BotUtils.sendErrorMessage(channel, "You must be at least level **40** to set your name emoji!"
                    + " You can view your progress with `!lvl`.");
            return;
        }
        
        //EmojiManager makes it easy to check for emoji
        if (EmojiManager.isEmoji(emoji)) {
            //some emojis take up 2 characters
            if (emoji.length() == 2) {
                user.setEmoji(Character.toCodePoint(emoji.charAt(0), emoji.charAt(1)));
            } else {
                user.setEmoji(emoji.codePointAt(0));
            }
            BotUtils.sendInfoMessage(channel, "Set your name emoji to " + emoji);
        } else if (emoji.toLowerCase().equals("none")) {
            user.setEmoji(0);
            BotUtils.sendInfoMessage(channel, "Your name emoji has been removed.");
        } else {
            BotUtils.sendErrorMessage(channel, "Could not parse an emoji from input.");
            return;
        }
        NameManager.formatNameOfUser(message.getGuild(), user);
        UserManager.saveDatabase();
    }
    
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "[emoji]", "Set an emoji next to your name."
                + " Providing \"none\" as the parameter will remove your current emoji."            
                + "\n*(Level 40+ or Prestiged)*");
    }
    
}
