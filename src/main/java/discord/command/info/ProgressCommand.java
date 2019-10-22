package discord.command.info;

import discord.util.BotUtils;
import discord.data.UserManager;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.data.object.XPChecker;
import discord.data.object.user.Progress;
import discord.util.ProfileBuilder;
import discord.data.object.user.User;
import java.util.List;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceState;

public class ProgressCommand extends AbstractCommand{
    
    public ProgressCommand() {
        super(new String[] {"progress", "prog", "level", "lvl", "rank", "xp"}, 0, CommandCategory.INFO);
    }
    
    @Override
    public void execute(IMessage message, String[] args) {
        if (args.length > 0) {
            List<IUser> mentions = message.getMentions();
            if (!mentions.isEmpty()) {
                if (UserManager.databaseContainsDUser(mentions.get(0))) {
                    BotUtils.sendEmbedMessage(message.getChannel(), buildProgressInfo(message.getGuild(), 
                        UserManager.getDBUserFromDUser(mentions.get(0))));
                } else {
                    BotUtils.sendErrorMessage(message.getChannel(), "Couldn't find that user in the database. Are they a bot?");
                }              
            } else {
                BotUtils.sendErrorMessage(message.getChannel(), "Couldn't parse a user. Please @mention them.");
            }
        } else {
            BotUtils.sendEmbedMessage(message.getChannel(), 
                    buildProgressInfo(message.getGuild(), UserManager.getDBUserFromMessage(message)));
        }
    }
    
    private EmbedObject buildProgressInfo(IGuild guild, User user) {
        ProfileBuilder builder = new ProfileBuilder(guild, user);
        Progress prog = user.getProgress();
        builder.addRank();
        builder.addLevel();
        if (prog.getPrestige().isPrestiged()) {
            builder.addPrestige();
        }
        if (prog.getReincarnation().isReincarnated()) {
            builder.addReincarnation();
        }
        if (prog.isNotMaxLevel()) {
            builder.addXPProgress();
            IVoiceState vState = guild.getUserByID(user.getDiscordID()).getVoiceStateForGuild(guild);
            if (vState.getChannel() != null && !XPChecker.voiceStateIsInvalid(vState)) { //Messy but oh well right?
                List<IUser> voiceUsers = vState.getChannel().getConnectedUsers();
                voiceUsers.removeIf(dUser -> dUser.isBot() 
                        || XPChecker.voiceStateIsInvalid(dUser.getVoiceStateForGuild(guild)));
                if (voiceUsers.size() >= 2) {
                    builder.addXPRate(voiceUsers.size());
                }
            }
            builder.addBarProgressToNextLevel();  
            if (!prog.getPrestige().isMax()) {
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
