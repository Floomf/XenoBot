package discord;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.guild.member.UserJoinEvent;
import sx.blah.discord.handle.obj.ActivityType;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.StatusType;

public class EventsHandler {
    
    @EventSubscriber
    public void onReadyEvent(ReadyEvent event) {
        IDiscordClient client = event.getClient();
        IGuild guild = client.getGuildByID(BotUtils.REALM_ID);
       
        client.changePresence(StatusType.ONLINE, ActivityType.WATCHING, BotUtils.VERSION);
  
        UserManager.createDatabase(guild);       
        XPHandler.startChecker(guild);      
    }
    
    @EventSubscriber
    //Add user to database when they join the server
    public void onUserJoinEvent(UserJoinEvent event) {
       IGuild guild = event.getGuild();
       IUser user = event.getUser();
       if (guild.getLongID() == BotUtils.REALM_ID && !user.isBot()) {
           UserManager.addUserToDatabase(event.getUser(), guild);
       }
    }
    
}
