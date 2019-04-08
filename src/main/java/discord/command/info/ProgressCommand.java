package discord.command.info;

import discord.util.BotUtils;
import discord.core.command.CommandHandler;
import discord.data.UserManager;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.util.ProfileBuilder;
import discord.data.object.User;
import java.util.List;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class ProgressCommand extends AbstractCommand{
    
    public ProgressCommand() {
        super(new String[] {"progress", "prog", "level", "lvl", "rank", "xp"}, 0, CommandCategory.INFO);
    }
    
    @Override
    public void execute(IMessage message, String[] args) {
        User user;
        if (args.length > 0) {
            List<IUser> mentions = message.getMentions();
            if (!mentions.isEmpty()) {
                user = UserManager.getDBUserFromDUser(mentions.get(0));
            } else {
                String name = CommandHandler.combineArgs(0, args);
                user = UserManager.getDBUserFromName(name);
            }
        } else {
            user = UserManager.getDBUserFromMessage(message);
        }
        if (user == null) {
            BotUtils.sendErrorMessage(message.getChannel(),
                    "Specified user was not found in the database.");
            return;
        }
        BotUtils.sendEmbedMessage(message.getChannel(),
                buildProgressInfo(message.getGuild(), user));
    }
    
    private EmbedObject buildProgressInfo(IGuild guild, User user) {
        ProfileBuilder builder = new ProfileBuilder(guild, user);
        
        builder.addRank();
        builder.addLevel();
        if (user.getProgress().getPrestige().isPrestiged()) {
            builder.addPrestige();
        }
        if (user.getProgress().getReincarnation().isReincarnated()) {
            builder.addReincarnation();
        }
        if (user.getProgress().isNotMaxLevel()) {
            builder.addXPProgress();
            builder.addBarProgressToNextLevel();  
            if (!user.getProgress().getPrestige().isMax()) {
                builder.addBarProgressToMaxLevel();
            }
        }
        return builder.build();
    }
    
    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "[nickname/@mention]", "View you or another user's level progress.");
    }
    
}
