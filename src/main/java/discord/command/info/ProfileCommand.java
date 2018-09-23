package discord.command.info;

import discord.BotUtils;
import discord.CommandHandler;
import discord.LevelManager;
import discord.UserManager;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import sx.blah.discord.handle.obj.IMessage;

public class ProfileCommand extends AbstractCommand {
    
    public ProfileCommand() {
        super(new String[] {"profile", "info", "stats"}, 0, CommandCategory.INFO);
    }
    
    //code copy and paste
    public void execute(IMessage message, String[] args) {
        long id;
        if (args.length > 0) {
            String name = CommandHandler.combineArgs(0, args);
            id = UserManager.getUserIDFromName(name);
            if (id == -1L) {
                BotUtils.sendErrorMessage(message.getChannel(), 
                        "Specified user was not found in the database.");
                return;
            }
        } else {
            id = message.getAuthor().getLongID();
        }
        BotUtils.sendEmbedMessage(message.getChannel(), 
                    LevelManager.buildFullUserInfo(message.getGuild(), UserManager.getUserFromID(id),
                    message.getGuild().getUserByID(id)));
    }
    
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "[name]", 
                "View you or another user's detailed profile.");
    }
    
}
