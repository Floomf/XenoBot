package discord.commands.fun;

import discord.BotUtils;
import discord.commands.AbstractCommand;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.apache.commons.io.FileUtils;
import sx.blah.discord.handle.obj.IMessage;

public class CatCommand extends AbstractCommand {
    
    public CatCommand() {
        super(new String[] {"cat", "kitty"}, 1, false);
    }
    
    public void execute(IMessage message, String[] args) {
        args[0] = args[0].toLowerCase();
        if (args[0].equals("pic") || args[0].equals("gif")) {
            String type = args[0];
            if (type.equals("pic")) {
                type = "png";
            }
            File f = new File("cat." + type);
            try {
                FileUtils.copyURLToFile(new URL( //shh, close your eyes
                        "http://thecatapi.com/api/images/get?format=src&api_key=MjA2OTcy&type=" + type), f);
                message.getChannel().sendFile(f);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else {
            BotUtils.sendErrorMessage(message.getChannel(), 
                    "Unknown media type. Type `!cat` for help.");
        }
    }
    
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "[pic/gif]", "View a random cat picture/gif");
    }
    
}
