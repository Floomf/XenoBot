package discord.command.admin;

import discord.util.BotUtils;
import discord.core.command.CommandHandler;
import discord.data.UserManager;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.data.object.User;
import java.util.List;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class SetNickCommand extends AbstractCommand {

    public SetNickCommand() {
        super(new String[]{"setnick", "changenick", "setname", "sn"}, 2, CommandCategory.ADMIN);
    }

    @Override
    public void execute(IMessage message, String[] args) {
        IChannel channel = message.getChannel();
        List<IUser> users = message.getMentions();
        if (users.isEmpty()) {
            BotUtils.sendErrorMessage(channel, "Could not parse a user. Please @mention them.");
        }
        User userToChange = UserManager.getDBUserFromDUser(users.get(0));
        if (userToChange == null) {
            BotUtils.sendErrorMessage(channel, "Specified user was not found in the database.");
            return;
        }
        String nick = BotUtils.validateNick(CommandHandler.combineArgs(1, args));
        if (nick.isEmpty()) {
            BotUtils.sendErrorMessage(channel, "The nickname can only contain basic letters and symbols.");
            return;
        }
        
        if (UserManager.databaseContainsName(nick)) {
            BotUtils.sendErrorMessage(channel, "Sorry, but that nickname is already taken.");
            return;
        }
        
        userToChange.getName().setNick(nick, message.getGuild());
        BotUtils.sendInfoMessage(channel, "Nickname updated to " + nick);
        UserManager.saveDatabase();
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "[@mention] [new nickname]",
                "Change the nickname of a user on this guild.");
    }

}
