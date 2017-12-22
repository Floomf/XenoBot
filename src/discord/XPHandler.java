package discord;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;

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
            users.removeIf(user -> user.isBot() 
                    || user.getVoiceStateForGuild(guild).isSelfDeafened() 
                    || user.getVoiceStateForGuild(guild).isSelfMuted()
                    || user.getVoiceStateForGuild(guild).isMuted());
            if (users.size() >= 2) {
                int xp = 5 * users.size() + 10; // min 300/hr
                for (IUser user : users) {  
                    LevelManager.addUserXP(guild, user.getLongID(), xp);
                    System.out.println("Gave " + xp + "xp to " + user.getName());                    
                }
                BotUtils.sendMessage(guild.getChannelByID(250084663618568192L), 
                        String.format("```py\n+%dXP [%s]```", xp, buildNames(users, guild)));
                UserManager.saveDatabase();
            }          
        }
    }
    
    private static String buildNames(List<IUser> users, IGuild guild) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < users.size() - 1; i++) {
            sb.append(users.get(i).getNicknameForGuild(guild));
            sb.append(", ");
        }
        sb.append(users.get(users.size() - 1));
        return sb.toString();
    }
}
