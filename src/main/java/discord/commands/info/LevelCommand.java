package discord.commands.info;

import discord.BotUtils;
import discord.LevelManager;
import discord.UserManager;
import discord.commands.AbstractCommand;
import sx.blah.discord.handle.obj.IMessage;

public class LevelCommand extends AbstractCommand{
    
    public LevelCommand() {
        super(new String[] {"level", "lvl", "info", "xp", "stats"}, 0, false);
    }
    
    public void execute(IMessage message, String[] args) {
        long id;
        if (args.length > 0) {
            String name = BotUtils.combineArgs(0, args);
            id = UserManager.getUserIDFromName(name);
            if (id == -1L) {
                BotUtils.sendErrorMessage(message.getChannel(), 
                        "Specified user was not found in the database.");;
            }
        } else {
            id = message.getAuthor().getLongID();
            BotUtils.sendEmbedMessage(message.getChannel(), 
                    LevelManager.buildInfo(UserManager.getUserFromID(id),
                    message.getGuild().getUserByID(id)));
        }
    }
    
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "[name]", "View you or another user's level progress.");
    }
    
}
