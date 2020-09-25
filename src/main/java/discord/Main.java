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
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class Main {

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("Please run the jar with your bot token as the first argument.");
            System.in.read();
            System.exit(0);
        }
        //DiscordClient client = new DiscordClientBuilder(args[0]).build();

        DiscordClientBuilder.create(args[0]).build().withGateway(client -> {
            client.on(ReadyEvent.class) // Listen for ReadyEvent(s)
                    .map(event -> event.getGuilds().size()) // Get how many guilds the bot is in
                    .flatMap(size -> client.getEventDispatcher()
                            .on(GuildCreateEvent.class) // Listen for GuildCreateEvent(s)
                            .take(size) // Take only the first `size` GuildCreateEvent(s) to be received
                            .collectList()) // Take all received GuildCreateEvents and make it a List
                    .subscribe(EventsHandler::onReadyEvent);// All guilds have been received, client is fully connected

            client.on(MessageCreateEvent.class).subscribe(CommandHandler::onMessageEvent);

            client.on(MemberJoinEvent.class).subscribe(UserManager::onMemberJoinEvent);
            client.on(MemberLeaveEvent.class).subscribe(UserManager::onMemberLeaveEvent);

            client.on(ReactionAddEvent.class).subscribe(GameManager::onReactionAddEvent);

            client.on(VoiceStateUpdateEvent.class).subscribe(EventsHandler::onVoiceStateUpdateEvent);
            client.on(PresenceUpdateEvent.class).subscribe(EventsHandler::onPresenceUpdateEvent);

            return client.onDisconnect();
        }).block();

    }

}
