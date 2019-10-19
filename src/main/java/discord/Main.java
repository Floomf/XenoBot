package discord;

import discord.listener.EventsHandler;
import discord.listener.ReactionListener;
import discord.core.command.CommandHandler;
import java.io.IOException;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;

public class Main {

    public static void main(String[] args) throws DiscordException, IOException {
        if (args.length == 0) {
                System.out.println("Please run the jar with your bot token as the first argument.");
                System.in.read();
                System.exit(0);
        }
        IDiscordClient client = new ClientBuilder()
                .withToken(args[0])
                .login();
        client.getDispatcher().registerListeners(
                new CommandHandler(), new EventsHandler(), new ReactionListener());
    }

}
