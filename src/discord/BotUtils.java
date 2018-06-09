package discord;

import java.awt.Color;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.RequestBuffer;

public class BotUtils {  
  
    //hardcoded constants
    public static final String VERSION = "2.14.2";
    public static final String CMD_PREFIX = "!";
    //todo remove hardcoded id
    public static final long REALM_ID = 98236427971592192L; //The Realm long id
    public static final int XP_MULTIPLIER = 1;
    public static final int MAX_LEVEL = 80;
    public static final char[] PRESTIGE_SYMBOLS = {'✦','✸','❃','✠','❂','☬','♆','∰'};

    private static EmbedObject buildMessage(String title, String message, Color color) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.withColor(color);
        if (title.trim().length() > 0) builder.withTitle(title);
        builder.withDesc(message);
        return builder.build();
    }
    
    public static void sendMessage(IChannel channel, String header, String body, Color color) {
        RequestBuffer.request(() -> {           
            try{
                channel.sendMessage(buildMessage(header, body, color));
            } catch (DiscordException e){
                System.err.println("Message could not be sent with error: " + e);
            }
        }).get(); //.get() makes sure they send in order cause async??
    }
    
    public static void sendMessage(IChannel channel, String header, String body) {
        sendMessage(channel, header, body, Color.WHITE);
    }
    
    public static void sendMessage(IChannel channel, String message) {
        sendMessage(channel, "", message, Color.WHITE);
    }
    
    public static void sendEmbedMessage(IChannel channel, EmbedObject object) {
        RequestBuffer.request(() -> {           
            try{
                channel.sendMessage(object);
            } catch (DiscordException e){
                System.err.println("Message could not be sent with error: " + e);
            }
        }).get();
    }
    
    public static void sendInfoMessage(IChannel channel, String message) {
        sendMessage(channel, "Info", message, Color.GREEN);
    }
    
    public static void sendErrorMessage(IChannel channel, String message) {
        sendMessage(channel, "Error", message, Color.RED);
    }
    
    public static void sendUsageMessage(IChannel channel, String message) {
        sendMessage(channel, "Usage", message, Color.ORANGE);
    }
    
    public static void setNickname(IGuild guild, IUser user, String name) {
        RequestBuffer.request(() -> {
            try{
                guild.setUserNickname(user, name);
                System.out.println("Set nickname of " + user.getName() + " to " + name);
            } catch (DiscordException e){
                System.err.println("Name could not be set with error: " + e);
            }
        });
    }
    
    public static void setRoles(IGuild guild, IUser user, IRole[] roles) {
        RequestBuffer.request(() -> {
            try{
                guild.editUserRoles(user, roles);
            } catch (DiscordException e) {
                System.err.println("Role could not be set with error: " + e);
            }
        });
    }
    
}
