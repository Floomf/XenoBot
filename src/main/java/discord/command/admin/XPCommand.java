package discord.command.admin;

import discord.BotUtils;
import discord.LevelManager;
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
        
        if (!(type.equals("give") || type.equals("set"))) {
            BotUtils.sendErrorMessage(channel, "Could not identify an XP operation.");
            return;
        }
        
        //check user id
        try {
            user = UserManager.getUserFromID(Long.parseLong(args[1]));
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
        
        //all data is valid, so perform action based on type
        if (type.equals("give")) {
            user.addXP(xp, message.getGuild());
            BotUtils.sendInfoMessage(channel, "Gave " + user.getName() + " **" + xp + "**XP");
        } else if (type.equals("set")) { //TODO FIX
            //user.setXP(xp); //TODO handle this more safely?
            //LevelManager.checkXPOfUser(message.getGuild(), user);
            //BotUtils.sendInfoMessage(channel, "Set " + user.getName() + "'s XP to **" + xp + "**");
        }        
        UserManager.saveDatabase();
    }
    
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "[give/set] [userID] [amount]", 
                "Give or set a user's XP in the database."
                + "\n\nuserID - The user's long ID.");
    }
    
}
