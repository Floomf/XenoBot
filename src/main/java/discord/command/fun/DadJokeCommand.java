package discord.command.fun;

import discord.BotUtils;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import sx.blah.discord.handle.obj.IMessage;

public class DadJokeCommand extends AbstractCommand {
    
    public DadJokeCommand() {
        super(new String[] {"dadjoke", "dj"}, 0, CommandCategory.FUN);
    }
    
    public void execute(IMessage message, String[] args) {
        try {
            URL url = new URL("https://icanhazdadjoke.com/");
            HttpURLConnection hc = (HttpURLConnection) url.openConnection();
            hc.setRequestProperty("Accept", "text/plain");
            hc.setRequestProperty("User-Agent", "Discord Bot");
            hc.connect();
            BufferedReader br = new BufferedReader(new InputStreamReader(hc.getInputStream()));
            BotUtils.sendMessage(message.getChannel(), "Get this:", String.format("`%s`", br.readLine()));
        } catch (IOException e) {
             e.printStackTrace();
        }
    }
    
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "", "View a random dad joke.");
    }
    
}
