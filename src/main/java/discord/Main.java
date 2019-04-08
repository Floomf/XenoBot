package discord;

import discord.listener.EventsHandler;
import discord.listener.ReactionListener;
import discord.core.command.CommandHandler;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;

public class Main {

    public static void main(String[] args) throws DiscordException {
        File tokenFile = new File("token.txt");
        if (!tokenFile.exists()) {
            try {
                tokenFile.createNewFile();
                System.out.println("token.txt created.");
                System.out.println("Please paste your bot token into the file and restart the program.");
                System.out.println("Press enter to exit...");
                System.in.read();
                System.exit(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        IDiscordClient client = new ClientBuilder()
                .withToken(getTextFromFile(tokenFile))
                .login();
        client.getDispatcher().registerListeners(
                new CommandHandler(), new EventsHandler(), new ReactionListener());

    }

    public static String getTextFromFile(File file) {
        try {
            Scanner sc = new Scanner(file);
            return sc.nextLine();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

}
