package discord;

import discord4j.core.event.domain.PresenceUpdateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord.core.game.GameManager;
import discord.data.UserManager;
import discord.listener.EventsHandler;
import discord.core.command.CommandHandler;

import java.io.IOException;

import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.VoiceStateUpdateEvent;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.guild.MemberJoinEvent;
import discord4j.core.event.domain.guild.MemberLeaveEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class Main {

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("Please run the jar with your bot token as the first argument.");
            System.in.read();
            System.exit(0);
        }

        DiscordClientBuilder.create(args[0]).build().withGateway(client -> {
            client.on(GuildCreateEvent.class).subscribe(EventsHandler::onGuildCreateEvent);

            client.on(MessageCreateEvent.class).subscribe(CommandHandler::onMessageEvent);

            client.on(MemberJoinEvent.class).subscribe(UserManager::onMemberJoinEvent);
            client.on(MemberLeaveEvent.class).subscribe(UserManager::onMemberLeaveEvent);

            client.on(PresenceUpdateEvent.class).subscribe(EventsHandler::onPresenceUpdateEvent);

            return client.onDisconnect();
        }).block();

    }

}
