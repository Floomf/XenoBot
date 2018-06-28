package discord;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;

public class Main { 
       
    public static void main(String[] args) {
        File tokenFile = new File("token.txt");
        if (!tokenFile.exists()) {
            try {
                tokenFile.createNewFile();
            } catch (IOException e) {
                System.out.println("Error creating token.txt: " + e);
            }
            System.exit(0);
        }
        try {
            IDiscordClient client = new ClientBuilder()
                    .withToken(getTextFromFile(tokenFile))
                    .login();
            client.getDispatcher().registerListeners(
                    new CommandHandler(), new EventsHandler());           
        } catch (DiscordException e) {
            System.out.println(e);
        }          
    }      
    
    public static String getTextFromFile(File file) {
        try {
            Scanner sc = new Scanner(file);
            return sc.nextLine();
        } catch (FileNotFoundException e) {
            System.out.println(e);
        }
        return "";
    }
    
}
