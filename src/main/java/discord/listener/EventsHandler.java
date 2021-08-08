package discord.listener;

import discord.command.utility.TagCommand;
import discord.data.object.BirthdayScheduler;
import discord.manager.PollManager;
import discord.util.BotUtils;
import discord.util.DiscordColor;
import discord.util.MessageUtils;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.PresenceUpdateEvent;
import discord.core.command.CommandManager;
import discord.manager.UserManager;
import discord.data.object.XPScheduler;

import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;
import discord.data.object.user.DUser;
import discord4j.core.object.reaction.ReactionEmoji;

public class EventsHandler {

    public static final Snowflake THE_REALM_ID = Snowflake.of(98236427971592192L);

    //TODO seperate into different classes?
    public static void onGuildCreateEvent(GuildCreateEvent event) {
        if (event.getGuild().getId().equals(THE_REALM_ID)) {
            //event.getClient().updatePresence(Presence.online(Activity.listening("/help"))).block();
            BotUtils.BOT_AVATAR_URL = event.getClient().getSelf().block().getAvatarUrl();
            UserManager.createDatabase(event.getGuild());
            PollManager.loadPolls(event.getClient());
            new XPScheduler(event.getGuild()).checkAnyChannelHasEnoughUsers();
            new BirthdayScheduler(event.getGuild());
            TagCommand.GAMES_ROLE_POSITION = event.getGuild().getRoleById(TagCommand.GAMES_ROLE_ID).block().getRawPosition();

            //CommandManager.createCommands();
            //CommandManager.createInteractions(event.getClient().getRestClient().getApplicationId().block(), event.getClient().getRestClient());
        }
    }

    //if the user changes their discord username, we can force their old name as nickname if not done already
    public static void onPresenceUpdateEvent(PresenceUpdateEvent event) {
        /*if (event.getGuildId().equals(THE_REALM_ID)) {
            if (!event.getNewUsername().orElse(event.getOldUser().get().getUsername())
                    .equalsIgnoreCase(event.getOldUser().get().getUsername())) {
                DUser user = UserManager.getDUserFromUser(event.getUser().block());
                user.setGuildMember(event.getMember().block()); //we have to update guildmember so displayname updates correctly
                user.getName().verifyOnGuild();
            }


        }*/
    }

}
