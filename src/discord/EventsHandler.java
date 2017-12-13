package discord;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.guild.member.UserJoinEvent;
import sx.blah.discord.handle.impl.events.guild.voice.user.UserSpeakingEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

public class EventsHandler {
    
    @EventSubscriber
    public void onReadyEvent(ReadyEvent event) {
        IDiscordClient client = event.getClient();
        IGuild guild = client.getGuildByID(BotUtils.REALM_ID);
       
        client.online("Type !help");
               
        UserManager.createDatabase(guild);       
        XPHandler.startChecker(guild);
        guild.getVoiceChannelByID(298179139221848064L).leave();
    }
    
    @EventSubscriber
    public void onUserTalkEvent(UserSpeakingEvent event) {
        IUser user = event.getUser();
        if (event.isSpeaking()) {
            System.out.println(user.getName() + " is speaking");
        }
    }
    
    @EventSubscriber
    public void onUserJoinEvent(UserJoinEvent event) {
       IGuild guild = event.getGuild();
       IUser user = event.getUser();
       if (guild.getLongID() == BotUtils.REALM_ID && !user.isBot()) {
           UserManager.addUserToDatabase(event.getUser(), guild);
       }
    }
    
}
