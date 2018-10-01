package discord;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.guild.member.UserJoinEvent;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelJoinEvent;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelLeaveEvent;
import sx.blah.discord.handle.impl.events.shard.DisconnectedEvent;
import sx.blah.discord.handle.impl.events.shard.ReconnectFailureEvent;
import sx.blah.discord.handle.impl.events.shard.ResumedEvent;
import sx.blah.discord.handle.obj.ActivityType;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.handle.obj.StatusType;

public class EventsHandler {
    
    private final static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static ScheduledFuture<?> future;
    
    @EventSubscriber
    public void onReadyEvent(ReadyEvent event) throws IOException {
        IDiscordClient client = event.getClient();
        IGuild guild = client.getGuilds().get(0);
        final Properties properties = new Properties();
        properties.load(this.getClass().getClassLoader().getResourceAsStream("project.properties"));
        client.changePresence(StatusType.ONLINE, ActivityType.WATCHING, 
                properties.getProperty("version"));
  
        UserManager.createDatabase(guild);
        CommandManager.createCommands();
        
        if (anyChannelHasEnoughUsers(guild)) {
            System.out.println("Found a voice channel with users > 1, starting xp checker");
            future = scheduler.scheduleAtFixedRate(new XPChecker(client), 2, 2, TimeUnit.MINUTES);
        }
    }
    
    @EventSubscriber
    //Add user to database when they join the server
    public void onUserJoinEvent(UserJoinEvent event) {
       IUser user = event.getUser();
       if (!user.isBot()) {
           UserManager.addUserToDatabase(event.getUser(), event.getGuild());
       }
    }
    
    private boolean anyChannelHasEnoughUsers(IGuild guild) {
        List<IVoiceChannel> channels = guild.getVoiceChannels();
        channels.remove(guild.getAFKChannel());
        for (IVoiceChannel channel : channels) {
            List<IUser> users = channel.getConnectedUsers();
            users.removeIf(IUser::isBot);
            if (users.size() > 1) return true;
        }
        return false;
    }
    
    @EventSubscriber
    public void onUserVoiceChannelJoinEvent(UserVoiceChannelJoinEvent event) {
        List<IUser> users = event.getVoiceChannel().getConnectedUsers();
        users.removeIf(IUser::isBot);
        if (future.isDone() && event.getVoiceChannel().getConnectedUsers().size() > 1) {
            System.out.println("Voice channel users > 1, starting xp checker");
            future = scheduler.scheduleAtFixedRate(new XPChecker(event.getClient()), 2, 2, TimeUnit.MINUTES);
        }
    }
    
    //only works with one guild for bot
    @EventSubscriber
    public void onUserVoiceChannelLeaveEvent(UserVoiceChannelLeaveEvent event) {
        if (!anyChannelHasEnoughUsers(event.getGuild())) {
            System.out.println("All guild voice channel users <= 1, stopping xp checker");
            future.cancel(true);
        }
    }
    
    @EventSubscriber
    public void onShardDisconnectedEvent(DisconnectedEvent event) {
        //if (!future.isDone()) { 
            //System.out.println("Shard disconnected, shutting down active XP scheduler");
            //future.cancel(true);
       // }
    }       
    
    @EventSubscriber
    public void onShardResumedEvent(ResumedEvent event) {
        //if (future.isDone()) {
            //System.out.println("Shard resumed, starting XP scheduler");
            //future = scheduler.scheduleAtFixedRate(new XPChecker(
                    //event.getClient()), 2, 2, TimeUnit.MINUTES);
        //}
    }
    
    @EventSubscriber
    public void onReconnectFailureEvent(ReconnectFailureEvent event) {
        if (event.isShardAbandoned()) {
            System.exit(1);
        }
    }
    
}
