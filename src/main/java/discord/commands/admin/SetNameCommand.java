package discord.commands.admin;

import discord.BotUtils;
import discord.NameManager;
import discord.UserManager;
import discord.commands.AbstractCommand;
import discord.objects.User;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;

public class SetNameCommand extends AbstractCommand{
    
    public SetNameCommand() {
        super(new String[] {"setname", "changename"}, 2, true);
    }
    
    public void execute(IMessage message, String[] args) {
        User userToChange = UserManager.getUserFromID(Long.parseLong(args[0]));
        IChannel channel = message.getChannel();
        if (userToChange == null) {
            BotUtils.sendErrorMessage(channel,
                    "Specified ID was not found in the database.");
            return;
        }
        args[1] = BotUtils.combineArgs(1, args);
        NameManager.setNameOfUser(message.getGuild(), userToChange, args[1]);
        BotUtils.sendInfoMessage(channel, "Name set to " + args[1]);
        return;
    }
    
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "[userID] [new name]", 
                "Change the name of a user in the database."
                + "\n\nuserID - The user's long ID.");
    }
    
}
