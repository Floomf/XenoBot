package discord;

import discord.command.admin.ThemeCommand;
import discord.command.utility.FocusCommand;
import discord.core.command.CommandManager;
import discord4j.common.store.Store;
import discord4j.common.store.legacy.LegacyStoreLayout;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.PresenceUpdateEvent;
import discord.manager.UserManager;
import discord.listener.EventsHandler;
import discord.core.command.CommandHandler;

import java.io.IOException;

import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.guild.MemberJoinEvent;
import discord4j.core.event.domain.guild.MemberLeaveEvent;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.ModalSubmitInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.gateway.intent.Intent;
import discord4j.gateway.intent.IntentSet;
import discord4j.rest.request.RouteMatcher;
import discord4j.rest.response.ResponseFunction;
import discord4j.rest.route.Routes;
import discord4j.store.jdk.JdkStoreService;
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
        //CommandManager.registerSlashCommands(client);
        /*client.getGuilds().map(GuildFields::id)
                .map(Snowflake::of)
                .collectList().block()
                .forEach(id -> CommandManager.createGuildSlashCommands(client, id));*/
        GatewayDiscordClient gateway = client.gateway().setDisabledIntents(IntentSet.of(Intent.GUILD_PRESENCES))
                //.setStore(Store.fromLayout(LegacyStoreLayout.of(new JdkStoreService())))
                .login().block();

        gateway.on(GuildCreateEvent.class).subscribe(EventsHandler::onGuildCreateEvent);
        gateway.on(MessageCreateEvent.class).subscribe(CommandHandler::onMessageEvent);

        gateway.on(MemberJoinEvent.class).subscribe(UserManager::onMemberJoinEvent);
        gateway.on(MemberLeaveEvent.class).subscribe(UserManager::onMemberLeaveEvent);

        //gateway.on(PresenceUpdateEvent.class).subscribe(EventsHandler::onPresenceUpdateEvent);
        gateway.on(ChatInputInteractionEvent.class).subscribe(CommandHandler::onInteractionCreate);

        //support for focus channel feature
        gateway.on(ButtonInteractionEvent.class)
                .filter(e -> e.getMessageId().equals(FocusCommand.UNFOCUS_MESSAGE_ID))
                .subscribe(e -> e.getInteraction().getMember().get().removeRole(FocusCommand.FOCUS_ROLE_ID).block());

        gateway.on(ModalSubmitInteractionEvent.class)
                .filter(e -> e.getCustomId().matches("theme_submit"))
                .subscribe(ThemeCommand::onSubmitTheme);

        gateway.onDisconnect().block();

    }

}
