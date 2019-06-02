package discord.data;

import java.util.List;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
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
            checkUsers(channel.getConnectedUsers(), channel, guild);
        }
        if (saveCounter == 10) {
            UserManager.saveDatabase();
            saveCounter = 1;
        } else {
            saveCounter++;
        }
    }

    private void checkUsers(List<IUser> dUsers, IChannel channel, IGuild guild) {
        dUsers.removeIf(user -> user.isBot() || voiceStateIsInvalid(user.getVoiceStateForGuild(guild))); //only count real people that are "talking"
        if (dUsers.size() >= 2) {
            int amount = dUsers.size();
            dUsers.removeIf(dUser -> UserManager.getDBUserFromDUser(dUser).getProgress().isMaxLevel());
            /*if (dUsers.isEmpty()) {
                return; //if all are max level
            }
            List<String> names = new ArrayList<>();
            dUsers.forEach(dUser -> names.add(UserManager.getDBUserFromID(dUser.getLongID()).getName().getNick()));
            DecimalFormat formatter = new DecimalFormat("#.###");
            EmbedBuilder builder = BotUtils.getBuilder(guild.getClient(), "+"
                    + (0.5 * GLOBAL_XP_MULTIPLIER * (amount + 13)) + "XP",
                    "`" + names.toString() + "`");
            builder.withFooterText(channel.getName());
            builder.withTimestamp(Instant.now());
            BotUtils.sendEmbedMessage(guild.getChannelsByName("log").get(0), builder.build());
            */
            dUsers.forEach(dUser -> UserManager.getDBUserFromDUser(dUser).getProgress().addPeriodicXP(amount, guild));
        }
    }
    
    public static boolean voiceStateIsInvalid(IVoiceState state) {
        return (state.isSelfDeafened() || state.isSelfMuted() || state.isDeafened() || state.isMuted());
    }
}
