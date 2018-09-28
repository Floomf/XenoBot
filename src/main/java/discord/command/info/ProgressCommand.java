package discord.command.info;

import discord.BotUtils;
import discord.CommandHandler;
import discord.UserManager;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.object.ProfileBuilder;
import discord.object.Progress;
import discord.object.User;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;

public class ProgressCommand extends AbstractCommand{
    
    public ProgressCommand() {
        super(new String[] {"progress", "prog", "level", "lvl"}, 0, CommandCategory.INFO);
    }
    
    public void execute(IMessage message, String[] args) {
        long id;
        if (args.length > 0) {
            String name = CommandHandler.combineArgs(0, args);
            id = UserManager.getDBUserIDFromName(name);
            if (id == -1L) {
                BotUtils.sendErrorMessage(message.getChannel(), 
                        "Specified user was not found in the database.");
                return;
            }
        } else {
            id = message.getAuthor().getLongID();
        }
        BotUtils.sendEmbedMessage(message.getChannel(), 
                    buildProgressInfo(message.getGuild(), UserManager.getDBUserFromID(id)));
    }
    
    private EmbedObject buildProgressInfo(IGuild guild, User user) {
        ProfileBuilder builder = new ProfileBuilder(guild, user);
        builder.addLevel();
        if (user.getProgress().getPrestige().getNumber() > 0) {
            builder.addPrestige();
        }
        builder.addXPProgress();
        if (user.getProgress().getLevel() < Progress.MAX_LEVEL) {
            builder.addBarProgressToNextLevel();
            builder.addBarProgressToMaxLevel();
        }
        return builder.build();
    }
    
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "[name]", "View you or another user's level progress.");
    }
    
}
