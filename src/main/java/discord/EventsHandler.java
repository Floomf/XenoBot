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
import sx.blah.discord.handle.impl.events.guild.member.UserLeaveEvent;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelJoinEvent;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelLeaveEvent;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelMoveEvent;
import sx.blah.discord.handle.impl.events.shard.ReconnectFailureEvent;
import sx.blah.discord.handle.impl.events.user.UserUpdateEvent;
import sx.blah.discord.handle.obj.ActivityType;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.handle.obj.StatusType;

public class EventsHandler {
    
    private final static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static ScheduledFuture<?> future;
    
    //TODO seperate into different classes?
    
    @EventSubscriber
    public void onReadyEvent(ReadyEvent event) throws IOException {
        IDiscordClient client = event.getClient();
        IGuild guild = client.getGuilds().get(0);
        final Properties properties = new Properties();
        properties.load(this.getClass().getClassLoader().getResourceAsStream("project.properties"));
        client.changePresence(StatusType.ONLINE, ActivityType.LISTENING, 
                properties.getProperty("version"));
        UserManager.createDatabase(guild);
        CommandManager.createCommands();
        
        if (anyChannelHasEnoughUsers(guild)) {
            System.out.println("Found a voice channel with users > 1, starting xp checker");
            future = scheduler.scheduleAtFixedRate(new XPChecker(client), 2, 2, TimeUnit.MINUTES);
        }
    }
    
    @EventSubscriber
    //if the user changes their discord username, we can force a nickname if not done already
    public void onUserUpdateEvent(UserUpdateEvent event) {
        if (!event.getNewUser().getName().equalsIgnoreCase(event.getOldUser().getName())) {
            UserManager.getDBUserFromDUser(event.getUser()).getName()
                    .verify(event.getClient().getGuilds().get(0)); //temporary
        }
    }
    
    @EventSubscriber
    //Add user to database when they join the server
    public void onUserJoinEvent(UserJoinEvent event) {
       IUser dUser = event.getUser();
       if (!dUser.isBot()) {
           UserManager.handleUserJoin(event.getUser(), event.getGuild());
       }
    }   
    
    @EventSubscriber
    public void onUserLeaveEvent(UserLeaveEvent event) {
        IUser dUser = event.getUser();
        if (!dUser.isBot()) {
            UserManager.handleUserLeave(dUser, event.getGuild());
        }
    }
    
    @EventSubscriber
    public void onUserVoiceChannelJoinEvent(UserVoiceChannelJoinEvent event) {
        List<IUser> users = event.getVoiceChannel().getConnectedUsers();
        users.removeIf(IUser::isBot);
        if ((future == null || future.isDone()) && users.size() > 1) {
            System.out.println("Voice channel users > 1, starting xp checker");
            future = scheduler.scheduleAtFixedRate(new XPChecker(event.getClient()), 2, 2, TimeUnit.MINUTES);
        }
    }
    
    @EventSubscriber
    public void onUserVoiceChannelMoveEvent(UserVoiceChannelMoveEvent event) {
        checkAnyChannelHasEnoughUsers(event.getGuild());
    }
    
    //only works with one guild for bot
    @EventSubscriber
    public void onUserVoiceChannelLeaveEvent(UserVoiceChannelLeaveEvent event) {
        checkAnyChannelHasEnoughUsers(event.getGuild());
    }
    
    @EventSubscriber
    public void onReconnectFailureEvent(ReconnectFailureEvent event) {
        if (event.isShardAbandoned()) {
            System.exit(1);
        }
    }
    
    private void checkAnyChannelHasEnoughUsers(IGuild guild) {
        boolean hasEnough = anyChannelHasEnoughUsers(guild);
        if ((future != null && !future.isDone()) && !hasEnough) {
            System.out.println("All guild voice channel users <= 1, stopping xp checker");
            future.cancel(true);
        } else if ((future == null || future.isDone()) && hasEnough) {
            System.out.println("Voice channel users > 1, starting xp checker");
            future = scheduler.scheduleAtFixedRate(new XPChecker(guild.getClient()), 2, 2, TimeUnit.MINUTES);
        }
    }
    
    private boolean anyChannelHasEnoughUsers(IGuild guild) {
        List<IVoiceChannel> channels = guild.getVoiceChannels();
        channels.remove(guild.getAFKChannel());
        for (IVoiceChannel channel : channels) {
            if (channelHasEnoughUsers(channel)) return true;
        }
        return false;
    }
    
    private boolean channelHasEnoughUsers(IVoiceChannel channel) {
        List<IUser> users = channel.getConnectedUsers();
        users.removeIf(IUser::isBot);
        return (users.size() > 1);
    }
    
}
