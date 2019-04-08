package discord.command.admin;

import discord.util.BotUtils;
import discord.data.UserManager;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.data.object.User;
import java.util.List;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class XPCommand extends AbstractCommand {
    
    public XPCommand() {
        super(new String[] {"givexp", "giveexp", "gxp"}, 2, CommandCategory.ADMIN); 
    }
    
    @Override
    public void execute(IMessage message, String[] args) {
        IChannel channel = message.getChannel();
        List<IUser> users = message.getMentions();
        double xp;
        
        if (users.isEmpty()) {
            BotUtils.sendErrorMessage(channel, 
                    "Could not identify any user. Please @mention at least one.");
            return;
        }
        
        //check xp amount
        try {
            xp = Double.parseDouble(args[0]);
            if (xp > 10000.0) xp = 10000.0; 
        } catch (NumberFormatException ex) {
            BotUtils.sendErrorMessage(channel, "Could not parse the specified XP amount.");
            return;
        }
        
        for (IUser dUser : users) {
            User user = UserManager.getDBUserFromDUser(dUser);
            if (user == null) continue;
            user.getProgress().addXP(xp, message.getGuild());
            BotUtils.sendInfoMessage(channel, "Gave " + user.getName().getNick() + " **" + xp + "**XP");         
        }   
        UserManager.saveDatabase(); //Will still save if no users were found, fix it sometime
    }
    
    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "[amount] [@mentions]", 
                "Give users XP in the database.");
    }
    
}
