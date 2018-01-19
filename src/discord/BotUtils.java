package discord;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RequestBuffer;

public class BotUtils {  
    
    //hardcoded constants
    public static final String VERSION = "2.5.0";
    public static final String CHANGEDATE = "1/18/18";
    public static final String CMD_PREFIX = "!";
    public static final long REALM_ID = 98236427971592192L; //The Realm long id
    public static final int XP_MULTIPLIER = 1;
    
    public static void sendMessage(IChannel channel, String message) {
        RequestBuffer.request(() -> {           
            try{
                channel.sendMessage(message);
            } catch (DiscordException e){
                System.err.println("Message could not be sent with error: " + e);
            }
        }).get(); //.get() makes sure they send in order cause async??
    }
    
    public static void sendMessage(IChannel channel, String header, String body) {
        sendMessage(channel, String.format("**%s**```%s```", header, body));
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
        sendMessage(channel, "Info", message);
    }
    
    public static void sendErrorMessage(IChannel channel, String message) {
        sendMessage(channel, "Error", message);
    }
    
    public static void sendUsageMessage(IChannel channel, String message) {
        sendMessage(channel, "Usage", message);
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
