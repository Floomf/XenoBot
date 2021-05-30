package discord;

import discord.command.utility.FocusCommand;
import discord.core.command.CommandManager;
import discord.util.DiscordColor;
import discord.util.MessageUtils;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.InteractionCreateEvent;
import discord4j.core.event.domain.PresenceUpdateEvent;
import discord.manager.UserManager;
import discord.listener.EventsHandler;
import discord.core.command.CommandHandler;

import java.io.IOException;

import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.guild.MemberJoinEvent;
import discord4j.core.event.domain.guild.MemberLeaveEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.discordjson.json.GuildFields;
import discord4j.rest.request.RouteMatcher;
import discord4j.rest.response.ResponseFunction;
import discord4j.rest.route.Routes;
import kong.unirest.Unirest;

public class Main {

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("Please run the jar with your bot token as the first argument.");
            System.in.read();
            System.exit(0);
        }
        Unirest.config().enableCookieManagement(false);

        DiscordClient client = DiscordClientBuilder.create(args[0])
                .onClientResponse(ResponseFunction.emptyOnErrorStatus(RouteMatcher.route(Routes.REACTION_CREATE), 403))
                .onClientResponse(ResponseFunction.emptyOnErrorStatus(RouteMatcher.route(Routes.REACTION_DELETE), 403))
                .onClientResponse(ResponseFunction.emptyOnErrorStatus(RouteMatcher.route(Routes.MESSAGE_CREATE), 403))
                .onClientResponse(ResponseFunction.emptyOnErrorStatus(RouteMatcher.route(Routes.MESSAGE_DELETE), 403))
                .onClientResponse(ResponseFunction.emptyOnErrorStatus(RouteMatcher.route(Routes.MESSAGE_DELETE_BULK), 403))
                .build();

        CommandManager.createCommands();
        //CommandManager.createSlashCommands(client);
        /*client.getGuilds().map(GuildFields::id)
                .map(Snowflake::of)
                .collectList().block()
                .forEach(id -> CommandManager.createGuildSlashCommands(client, id));*/

        client.withGateway(c -> {
                    c.on(GuildCreateEvent.class).subscribe(EventsHandler::onGuildCreateEvent);
                    c.on(MessageCreateEvent.class).subscribe(CommandHandler::onMessageEvent);

                    c.on(MemberJoinEvent.class).subscribe(UserManager::onMemberJoinEvent);
                    c.on(MemberLeaveEvent.class).subscribe(UserManager::onMemberLeaveEvent);

                    c.on(PresenceUpdateEvent.class).subscribe(EventsHandler::onPresenceUpdateEvent);
                    c.on(InteractionCreateEvent.class).subscribe(CommandHandler::onInteractionCreate);

                    //support for focus channel feature
                    c.on(ReactionAddEvent.class).filter(e -> e.getMessageId().equals(FocusCommand.UNFOCUS_MESSAGE_ID)
                            && !e.getUser().block().isBot())
                            .subscribe(e -> {
                                e.getMember().get().removeRole(FocusCommand.FOCUS_ROLE_ID).block();
                                e.getMessage().block().removeReaction(e.getEmoji(), e.getUserId()).block();
                            });
                    return c.onDisconnect();
        }).block();

    }

}
