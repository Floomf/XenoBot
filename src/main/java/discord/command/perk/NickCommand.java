package discord.command.perk;

import discord.BotUtils;
import discord.CommandHandler;
import discord.UserManager;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.object.User;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;

public class NickCommand extends AbstractCommand {

    private static final int LEVEL_REQUIRED = 60; 
    
    public NickCommand() {
        super(new String[]{"nick", "name", "nickname"}, 1, LEVEL_REQUIRED, CommandCategory.PERK);
    }

    public void execute(IMessage message, String[] args) {
        User user = UserManager.getDBUserFromMessage(message);
        IChannel channel = message.getChannel();
        String nick = BotUtils.validateNick(CommandHandler.combineArgs(0, args));
        
        if (nick.isEmpty()) {
            BotUtils.sendErrorMessage(channel, "Your nickname can only contain basic letters and symbols.");
            return;
        }

        if (UserManager.databaseContainsName(nick)) {
            BotUtils.sendErrorMessage(channel, "Sorry, but that nickname is already taken.");
            return;
        }
        
        user.getName().setNick(nick, message.getGuild());
        BotUtils.sendInfoMessage(channel, "Nickname updated. Pleasure to meet ya, " + nick + ".");
        UserManager.saveDatabase();
    }

    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "[new name]", "Change your nickname on this guild."
                + "\n*(Level 60+ or Prestiged)*");
    }

}
