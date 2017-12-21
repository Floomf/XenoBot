package discord;

import sx.blah.discord.util.DiscordException;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;

public class Main {
       
    public static void main(String[] args) {
        try {
            IDiscordClient client = new ClientBuilder()
                    .withToken("Mjg5MzMwMDQ3MDgzMzQ3OTY4.C8LlBw.F4LZfdtjOt-PIvAI5yU9pPanaGU")
                    .login();
            client.getDispatcher().registerListeners(
                    new CommandHandler(), new EventsHandler());           
        } catch (DiscordException e) {
            System.out.println(e);
        }          
    }      
    
}
