package discord.command.info;

import discord.util.BotUtils;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import java.awt.Color;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

public class InfoCommand extends AbstractCommand {
    
    private static final long START_TIME = System.currentTimeMillis();
    
    public InfoCommand() {
        super(new String[] {"info", "stats"}, 0, CommandCategory.INFO);
    }
    
    @Override
    public void execute(IMessage message, String[] args) {
        EmbedBuilder builder = BotUtils.getBaseBuilder(message.getClient());
        builder.withAuthorName(message.getClient().getOurUser().getName());
        builder.withThumbnail(message.getClient().getOurUser().getAvatarURL());
        builder.withColor(Color.CYAN);
        
        builder.appendField("Version 🏷", "`" + BotUtils.getVersion() + "`", true);
        builder.appendField("Uptime 🕓", "`" + formatElapsedTime(System.currentTimeMillis() - START_TIME) + "`", true);
        builder.appendField("Guilds 🏘", "`" + message.getClient().getGuilds().size() + "`", true);
        
        BotUtils.sendEmbedMessage(message.getChannel(), builder.build());
    }
    
    private static String formatElapsedTime(long millis) {
        String result = "";
        long totalSeconds = millis / 1000;
        
        //All of these are about getting the remainder units of time
        long s = totalSeconds % 60;
        long m = (totalSeconds / 60) % 60;
        long h = (totalSeconds / 60 / 60) % 24;
        long d = (totalSeconds / 60 / 60 / 24);      
        
        if (d > 0) {
            result += d + "d ";
        }
        if (h > 0) {
            result += h + "h ";
        }
        if (m > 0) {
            result += m + "m ";
        }
        if (s > 0) {
            result += s + "s ";
        }
        
        return result.trim(); //Way to get rid of last space
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "", "View various stats about Xeno.");
    }
    
}
