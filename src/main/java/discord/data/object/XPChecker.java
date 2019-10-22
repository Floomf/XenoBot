package discord.data.object;

import discord.data.UserManager;
import java.util.List;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.handle.obj.IVoiceState;

public class XPChecker implements Runnable {

    private final IDiscordClient client;
    private int saveCounter = 1;

    public XPChecker(IDiscordClient client) {
        this.client = client;
    }

    @Override
    public void run() {
        if (client.isReady()) {
            System.out.println("Checking all guild users to add xp");
            checkGuilds(client.getGuilds());
        } else {
            System.out.println(String.format("Client isn't ready, won't check users"));
        }
    }

    private void checkGuilds(List<IGuild> guilds) {
        for (IGuild guild : guilds) {
            checkVoiceChannels(guild);
        }
    }

    private void checkVoiceChannels(IGuild guild) {
        List<IVoiceChannel> channels = guild.getVoiceChannels();
        channels.removeIf(channel -> channel.equals(guild.getAFKChannel()));
        for (IVoiceChannel channel : channels) {
            checkUsers(channel.getConnectedUsers(), guild);
        }
        if (saveCounter == 10) {
            UserManager.saveDatabase();
            saveCounter = 1;
        } else {
            saveCounter++;
        }
    }

    private void checkUsers(List<IUser> dUsers, IGuild guild) {
        dUsers.removeIf(user -> user.isBot() || voiceStateIsInvalid(user.getVoiceStateForGuild(guild))); //only count real people that are "talking"
        if (dUsers.size() >= 2) {
            dUsers.removeIf(dUser -> UserManager.getDBUserFromDUser(dUser).getProgress().isMaxLevel());
            dUsers.forEach(dUser -> UserManager.getDBUserFromDUser(dUser).getProgress().addPeriodicXP(dUsers.size(), guild));
        }
    }
    
    public static boolean voiceStateIsInvalid(IVoiceState state) {
        return (state.isSelfDeafened() || state.isSelfMuted() || state.isDeafened() || state.isMuted());
    }
}
