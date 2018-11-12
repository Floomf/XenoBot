package discord.command.info;

import discord.BotUtils;
import discord.CommandHandler;
import discord.UserManager;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.object.Prestige;
import discord.object.ProfileBuilder;
import discord.object.User;
import java.util.List;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class ProgressCommand extends AbstractCommand{
    
    public ProgressCommand() {
        super(new String[] {"progress", "prog", "level", "lvl", "xp"}, 0, CommandCategory.INFO);
    }
    
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
        Prestige prestige = user.getProgress().getPrestige();
        builder.addLevel();
        if (user.getProgress().getPrestige().getNumber() > 0) {
            builder.addPrestige();
        }
        builder.addXPProgress();
        if (prestige.isMax() || !user.getProgress().isMaxLevel()) {
            builder.addBarProgressToNextLevel();
        }
        if (!prestige.isMax() && !user.getProgress().isMaxLevel()) {
            builder.addBarProgressToMaxLevel();
        }
        return builder.build();
    }
    
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "[nickname/@mention]", "View you or another user's level progress.");
    }
    
}
