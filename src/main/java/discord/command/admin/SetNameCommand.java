package discord.command.admin;

import discord.BotUtils;
import discord.CommandHandler;
import discord.UserManager;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.object.User;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;

public class SetNameCommand extends AbstractCommand{
    
    public SetNameCommand() {
        super(new String[] {"setname", "changename", "setnick"}, 2, CommandCategory.ADMIN);
    }
    
    public void execute(IMessage message, String[] args) {
        User userToChange = UserManager.getDBUserFromID(Long.parseLong(args[0]));
        IChannel channel = message.getChannel();
        if (userToChange == null) {
            BotUtils.sendErrorMessage(channel, "Specified ID was not found in the database.");
            return;
        }
        String nick = CommandHandler.combineArgs(1, args); //TODO validate nickname
        if (!UserManager.databaseContainsName(nick)) {
            userToChange.getName().setNick(nick, message.getGuild());
            BotUtils.sendInfoMessage(channel, "Nickname set to " + args[1]);
            UserManager.saveDatabase();
        } else {
            BotUtils.sendErrorMessage(channel, "Sorry, but that nickname is already taken.");    
        } 
    }
    
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "[userID] [new name]", 
                "Change the nickname of a user in the database."
                + "\n\nuserID - The user's long ID.");
    }
    
}
