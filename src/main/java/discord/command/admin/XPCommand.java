package discord.command.admin;

import discord.BotUtils;
import discord.UserManager;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.object.User;
import java.util.List;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class XPCommand extends AbstractCommand {
    
    public XPCommand() {
        super(new String[] {"givexp", "giveexp", "gxp"}, 2, CommandCategory.ADMIN); 
    }
    
    public void execute(IMessage message, String[] args) {
        IChannel channel = message.getChannel();
        List<IUser> users = message.getMentions();
        int xp;
        
        if (users.isEmpty()) {
            BotUtils.sendErrorMessage(channel, 
                    "Could not identify any user. Please @mention at least one.");
            return;
        }
        
        //check xp amount
        try {
            xp = Integer.parseInt(args[0]);
            if (xp > 10000) xp = 10000; 
        } catch (NumberFormatException ex) {
            BotUtils.sendErrorMessage(channel, "Could not parse the specified XP amount.");
            return;
        }
        
        for (IUser dUser : users) {
            User user = UserManager.getDBUserFromDUser(dUser);
            if (user == null) continue;
            user.getProgress().addXP(xp, message.getGuild());
            BotUtils.sendInfoMessage(channel, "Gave " + user.getName() + " **" + xp + "**XP");         
        }   
        UserManager.saveDatabase(); //Will still save if no users were found, fix it sometime
    }
    
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "[amount] [@mentions]", 
                "Give users XP in the database.");
    }
    
}
