package discord.listener;

import discord.util.BotUtils;
import discord4j.core.event.domain.PresenceUpdateEvent;
import discord.data.object.XPChecker;
import discord.core.command.CommandManager;
import discord.data.UserManager;
import discord.data.object.XPScheduler;

import java.util.List;

import discord4j.core.event.domain.VoiceStateUpdateEvent;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;
import discord.data.object.user.DUser;

public class EventsHandler {

    private static XPScheduler scheduler;

    //TODO seperate into different classes?

    public static void onReadyEvent(List<GuildCreateEvent> events) {
        Guild guild = events.get(0).getGuild();
        guild.getClient().updatePresence(Presence.online(Activity.listening("!help"))).block();
        BotUtils.BOT_AVATAR_URL = guild.getClient().getSelf().block().getAvatarUrl();

        UserManager.createDatabase(guild);
        CommandManager.createCommands();

        scheduler = new XPScheduler(new XPChecker(guild));

        scheduler.checkAnyChannelHasEnoughUsers(guild);
    }

    //if the user changes their discord username, we can force their old name as nickname if not done already
    public static void onPresenceUpdateEvent(PresenceUpdateEvent event) {
        if (!event.getNewUsername().orElse(event.getOldUser().get().getUsername())
                .equalsIgnoreCase(event.getOldUser().get().getUsername())) {
            DUser user = UserManager.getDUserFromUser(event.getUser().block());
            user.setGuildMember(event.getMember().block()); //we have to update guildmember so displayname updates correctly
            user.getName().verifyOnGuild();
        }
    }

    public static void onVoiceStateUpdateEvent(VoiceStateUpdateEvent event) {
        scheduler.checkAnyChannelHasEnoughUsers(event.getCurrent().getGuild().block());
    }

    /*
    @EventSubscriber
    public void onReconnectFailureEvent(ReconnectFailureEvent event) {
        if (event.isShardAbandoned()) {
            System.exit(1);
        }
    }*/

}
