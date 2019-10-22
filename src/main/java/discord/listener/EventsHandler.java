package discord.listener;

import discord.data.object.XPChecker;
import discord.core.command.CommandManager;
import discord.data.UserManager;
import discord.data.object.XPScheduler;
import java.io.IOException;
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
import sx.blah.discord.handle.obj.StatusType;

public class EventsHandler {

    private static XPScheduler scheduler;

    //TODO seperate into different classes?
    @EventSubscriber
    public void onReadyEvent(ReadyEvent event) throws IOException {
        IDiscordClient client = event.getClient();
        IGuild guild = client.getGuilds().get(0);
        client.changePresence(StatusType.ONLINE, ActivityType.LISTENING, "!help");
        
        UserManager.createDatabase(guild);
        CommandManager.createCommands();

        scheduler = new XPScheduler(new XPChecker(client));
        
        scheduler.checkAnyChannelHasEnoughUsers(guild);
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
    public void onUserJoinEvent(UserJoinEvent event) {
        if (!event.getUser().isBot()) {
            UserManager.handleUserJoin(event.getUser(), event.getGuild());
        }
    }

    @EventSubscriber
    public void onUserLeaveEvent(UserLeaveEvent event) {
        if (!event.getUser().isBot()) {
            UserManager.handleUserLeave(event.getUser(), event.getGuild());
        }
    }

    @EventSubscriber
    public void onUserVoiceChannelJoinEvent(UserVoiceChannelJoinEvent event) {
        scheduler.checkAnyChannelHasEnoughUsers(event.getGuild());
    }

    @EventSubscriber
    public void onUserVoiceChannelMoveEvent(UserVoiceChannelMoveEvent event) {
        scheduler.checkAnyChannelHasEnoughUsers(event.getGuild());
    }

    @EventSubscriber
    public void onUserVoiceChannelLeaveEvent(UserVoiceChannelLeaveEvent event) {
        scheduler.checkAnyChannelHasEnoughUsers(event.getGuild());
    }

    @EventSubscriber
    public void onReconnectFailureEvent(ReconnectFailureEvent event) {
        if (event.isShardAbandoned()) {
            System.exit(1);
        }
    }

}
