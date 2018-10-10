package discord.command.admin;

import discord.BotUtils;
import discord.UserManager;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.object.User;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;

public class XPCommand extends AbstractCommand {
    
    public XPCommand() {
        super(new String[] {"xp", "exp"}, 3, CommandCategory.ADMIN); 
    }
    
    public void execute(IMessage message, String[] args) {
        String type = args[0].toLowerCase();
        IChannel channel = message.getChannel();
        User user;
        int xp;
        
        if (!type.equals("give")) {
            BotUtils.sendErrorMessage(channel, "Could not identify an XP operation.");
            return;
        }
        
        //check user id
        try {
            user = UserManager.getDBUserFromID(Long.parseLong(args[1]));
        } catch (NumberFormatException ex) {
            BotUtils.sendErrorMessage(channel, "Could not parse the specified user ID.");
            return;
        }
        
        //check user exists
        if (user == null) {
            BotUtils.sendErrorMessage(channel, "Could not find the specified ID in the database");
            return;
        }
        
        //check xp amount
        try {
            xp = Integer.parseInt(args[2]);
        } catch (NumberFormatException ex) {
            BotUtils.sendErrorMessage(channel, "Could not parse the specified XP amount.");
            return; 
        }
        
        //all data is valid, so perform action
        if (xp > 10000) xp = 10000; 
        user.getProgress().addXP(xp, message.getGuild());
        BotUtils.sendInfoMessage(channel, "Gave " + user.getName() + " **" + xp + "**XP");
        UserManager.saveDatabase();
    }
    
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "[give] [userID] [amount]", 
                "Give or set a user's XP in the database."
                + "\n\nuserID - The user's long ID.");
    }
    
}
