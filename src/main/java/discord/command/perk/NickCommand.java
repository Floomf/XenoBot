package discord.command.perk;

import discord.util.BotUtils;
import discord.core.command.CommandHandler;
import discord.data.UserManager;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.data.object.User;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;

public class NickCommand extends AbstractCommand {

    public static final int LEVEL_REQUIRED = 40; 
    
    public NickCommand() {
        super(new String[]{"nick", "name", "nickname"}, 1, LEVEL_REQUIRED, CommandCategory.PERK);
    }

    @Override
    public void execute(IMessage message, String[] args) {
        User user = UserManager.getDBUserFromMessage(message);
        IChannel channel = message.getChannel();
        String nick = BotUtils.validateNick(CommandHandler.combineArgs(0, args));
        
        if (nick.isEmpty()) {
            BotUtils.sendErrorMessage(channel, "Your nickname can only contain basic letters and symbols.");
            return;
        }
  
        user.getName().setNick(nick, message.getGuild());
        BotUtils.sendInfoMessage(channel, "Nickname updated. Pleasure to meet ya, " + nick + ".");
        UserManager.saveDatabase();
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "[new name]", "Change your nickname on this guild.");
    }

}
