package discord;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.handle.obj.IVoiceState;

public class XPHandler {

    private final static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static void startChecker(IGuild guild) {
        final Runnable pinger = new Runnable() {
            public void run() {
               System.out.println("Checking users to add xp");
               checkUsers(guild);
            }
        };
        scheduler.scheduleAtFixedRate(pinger, 4, 4, TimeUnit.MINUTES);
    }

    private static void checkUsers(IGuild guild) {
        List<IVoiceChannel> channels = guild.getVoiceChannels();
        channels.removeIf(channel -> channel.equals(guild.getAFKChannel()));
        for (IVoiceChannel channel : channels) {
            List<IUser> users = channel.getConnectedUsers();
            users.removeIf(user -> user.isBot());
            users.removeIf(user -> user.getVoiceStateForGuild(guild).isSelfDeafened());
            if (users.size() >= 2) {
                List<String> namesToGive = new ArrayList<>();
                int xp = 5 * users.size() + 10; // min 300/hr
                for (IUser user : users) { 
                    IVoiceState state = user.getVoiceStateForGuild(guild);
                    
                    if (user.isBot() || state.isSelfMuted() || state.isMuted()) {
                        continue;
                    }
                    LevelManager.addUserXP(guild, user.getLongID(), xp);
                    namesToGive.add(UserManager.getUserName(user.getLongID()));
                    System.out.println("Gave " + xp + "xp to " + user.getName());
                }
                if (!namesToGive.isEmpty()) {
                    BotUtils.sendMessage(guild.getChannelByID(250084663618568192L), 
                            String.format("```py\n+%dXP %s```", xp, namesToGive.toString()));
                    UserManager.saveDatabase();
                }
            }
        }
    }
}
