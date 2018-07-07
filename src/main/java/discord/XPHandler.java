package discord;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;

public class XPHandler {

    private final static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE hh:mma");

    public static void startChecker(IGuild guild) {
        final Runnable pinger = new Runnable() {
            public void run() {
               System.out.println(String.format("[%s] Checking users to add xp",
                       LocalDateTime.now().format(formatter)));
               checkVoiceChannels(guild);
            }
        };
        scheduler.scheduleAtFixedRate(pinger, 2, 2, TimeUnit.MINUTES);
    }

    private static void checkVoiceChannels(IGuild guild) {
        List<IVoiceChannel> channels = guild.getVoiceChannels();
        channels.removeIf(channel -> channel.equals(guild.getAFKChannel()));
        for (IVoiceChannel channel : channels) {
            checkUsers(channel.getConnectedUsers(), channel, guild);
        }
    }
    
    private static void checkUsers(List<IUser> users, IChannel channel, IGuild guild) {
        users.removeIf(user -> user.isBot()
                || user.getVoiceStateForGuild(guild).isSelfDeafened()
                || user.getVoiceStateForGuild(guild).isSelfMuted()
                || user.getVoiceStateForGuild(guild).isMuted());
        if (users.size() >= 2) {
            List<String> names = new ArrayList<>();
            int xp = 1 * users.size() + 13; // min 450/hr
            users.removeIf(user -> UserManager.getUserLevel(
                    user.getLongID()) == LevelManager.MAX_LEVEL);
            for (IUser user : users) {
                String name = UserManager.getUserFromID(user.getLongID()).getName();
                LevelManager.addUserXPFromID(guild, user.getLongID(), xp);
                names.add(name);
                System.out.println("Gave " + xp + "xp to " + name);
            }
            BotUtils.sendMessage(guild.getChannelsByName("log").get(0),
                    String.format("+%dXP in %s (%s)", xp, 
                            channel.getName(), LocalDateTime.now().format(formatter)),
                            names.toString());
            UserManager.saveDatabase();
        }
    }
    
}
