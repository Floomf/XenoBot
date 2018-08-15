package discord.commands.info;

import discord.BotUtils;
import discord.CommandHandler;
import discord.LevelManager;
import discord.UserManager;
import discord.commands.AbstractCommand;
import discord.commands.CommandCategory;
import sx.blah.discord.handle.obj.IMessage;

public class LevelCommand extends AbstractCommand{
    
    public LevelCommand() {
        super(new String[] {"level", "lvl", "info", "stats"}, 0, CommandCategory.INFO);
    }
    
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
                    LevelManager.buildUserInfo(UserManager.getUserFromID(id),
                    message.getGuild().getUserByID(id)));
    }
    
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "[name]", "View you or another user's level progress.");
    }
    
}
