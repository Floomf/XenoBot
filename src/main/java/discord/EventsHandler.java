package discord;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.guild.member.UserJoinEvent;
import sx.blah.discord.handle.impl.events.shard.DisconnectedEvent;
import sx.blah.discord.handle.impl.events.shard.ResumedEvent;
import sx.blah.discord.handle.obj.ActivityType;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.StatusType;

public class EventsHandler {
    
    private final static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static ScheduledFuture<?> future;
    
    @EventSubscriber
    public void onReadyEvent(ReadyEvent event) {
        IDiscordClient client = event.getClient();
        IGuild guild = client.getGuilds().get(0);
        final Properties properties = new Properties();
        try {
            properties.load(this.getClass().getClassLoader()
                    .getResourceAsStream("project.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        client.changePresence(StatusType.ONLINE, ActivityType.WATCHING, 
                properties.getProperty("version"));
  
        UserManager.createDatabase(guild);      
        CommandManager.createCommands();
        future = scheduler.scheduleAtFixedRate(new XPChecker(client), 2, 2, TimeUnit.MINUTES);
    }
    
    @EventSubscriber
    //Add user to database when they join the server
    public void onUserJoinEvent(UserJoinEvent event) {
       IUser user = event.getUser();
       if (!user.isBot()) {
           UserManager.addUserToDatabase(event.getUser(), event.getGuild());
       }
    }
    
    @EventSubscriber
    public void onShardDisconnectedEvent(DisconnectedEvent event) {
        if (!future.isDone()) { 
            System.out.println("Shard disconnected, shutting down active XP scheduler");
            future.cancel(true);
        }
    }       
    
    @EventSubscriber
    public void onShardResumedEvent(ResumedEvent event) {
        if (future.isDone()) {
            System.out.println("Shard resumed, starting XP scheduler");
            future = scheduler.scheduleAtFixedRate(new XPChecker(
                    event.getClient()), 2, 2, TimeUnit.MINUTES);
        }
    }
    
}
