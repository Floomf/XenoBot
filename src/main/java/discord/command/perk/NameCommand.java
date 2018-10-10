package discord.command.perk;

import discord.BotUtils;
import discord.CommandHandler;
import discord.UserManager;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.object.User;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;

public class NameCommand extends AbstractCommand {
    
    public NameCommand() {
        super(new String[] {"name", "nick", "nickname"}, 1, CommandCategory.PERK);
    }
    
    public void execute(IMessage message, String[] args) {
        User user = UserManager.getDBUserFromMessage(message);
        IChannel channel = message.getChannel();
        if (!(user.getProgress().getTotalLevels() > 60)) {
            BotUtils.sendErrorMessage(channel, 
                    "You must be at least level **60** to change your nickname!"
                    + " You can view your progress with `!lvl`.");
            return;
        }
        String name = CommandHandler.combineArgs(0, args);
        if (name.length() > 17) { //name can't be too long
            name = name.substring(0, 16);
        }
        
        if (!name.matches("[\u0020-\u00FF]+")) { //regex for charcodes 32-255
            BotUtils.sendErrorMessage(channel, "Your nickname can only contain basic letters and symbols.");
            return;
        }

       if (!UserManager.databaseContainsName(name)) {
            user.getName().setNick(name, message.getGuild());
            BotUtils.sendInfoMessage(channel, "Nickname updated. Pleasure to meet ya, " + name + ".");
            UserManager.saveDatabase();
        } else {
            BotUtils.sendErrorMessage(channel, "Sorry, but that nickname is already taken.");    
        } 
    }
    
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "[new name]", "Change your nickname on this guild."
                + "\n*(Level 60+ or Prestiged)*");
    }
    
}
