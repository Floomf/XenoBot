package discord.command.info;

import discord.BotUtils;
import discord.CommandHandler;
import discord.UserManager;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.object.ProfileBuilder;
import discord.object.User;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IGuild;
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
            id = UserManager.getDBUserIDFromName(name);
            if (id == -1L) {
                BotUtils.sendErrorMessage(message.getChannel(), 
                        "Specified user was not found in the database.");
                return;
            }
        } else {
            id = message.getAuthor().getLongID();
        }
        BotUtils.sendEmbedMessage(message.getChannel(), buildProfileInfo(message.getGuild(), 
                            UserManager.getDBUserFromID(id)));
    }
    
    public EmbedObject buildProfileInfo(IGuild guild, User user) {
        ProfileBuilder builder = new ProfileBuilder(guild, user);
        boolean prestiged = (user.getProgress().getPrestige().getNumber() > 0);
        
        builder.addLevel();
        if (prestiged) builder.addPrestige();
        builder.addXPProgress();
        builder.addTotalXP();
        if (prestiged) {
            builder.addTotalLevel();
            builder.addBadgeCase();
        }
        builder.addBarProgressToMaxLevel();
        return builder.build();
    }
    
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "[name]", 
                "View you or another user's detailed profile.");
    }
    
}
