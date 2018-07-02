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
    public static final String CMD_PREFIX = "!";
    //todo remove hardcoded id
    public static final long REALM_ID = 98236427971592192L; //The Realm long id
    public static final int MAX_LEVEL = 80;
    public static final char[] PRESTIGE_SYMBOLS = {'✦','✸','❃','✠','❂','☬','♆','∰'};

    private static EmbedObject buildMessage(String title, String message, Color color) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.withColor(color);
        if (title.trim().length() > 0) builder.withTitle(title);
        builder.withDesc(message);
        return builder.build();
    }
    
    public static void sendMessage(IChannel channel, String outside, String header, String body, Color color) {
        RequestBuffer.request(() -> {           
            try{
                if (outside.trim().length() > 0) {
                    channel.sendMessage(outside, buildMessage(header, body, color));
                } else {
                    channel.sendMessage(buildMessage(header, body, color));
                }
            } catch (DiscordException e){
                System.err.println("Message could not be sent with error: " + e);
            }
        }).get(); //.get() makes sure they send in order cause async??
    }
    
    public static void sendMessage(IChannel channel, String header, String body, Color color) {
        sendMessage(channel, "", header, body, color);
    }
    
    public static void sendMessage(IChannel channel, String header, String body) {
        sendMessage(channel, "", header, body, Color.WHITE);
    }
    
    public static void sendMessage(IChannel channel, String message) {
        sendMessage(channel, "", "", message, Color.WHITE);
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
        sendMessage(channel, "", "Info", message, Color.GREEN);
    }
    
    public static void sendErrorMessage(IChannel channel, String message) {
        sendMessage(channel, "", "Error", message, Color.RED);
    }
    
    public static void sendUsageMessage(IChannel channel, String message) {
        sendMessage(channel, "", "Usage", message, Color.ORANGE);
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
    
    public static String combineArgs(int index, String[] args) {       
        for (int i = index + 1; i < args.length; i++) {
             args[index] += " " + args[i];
        }
        return args[index];
    }
    
    public static EmbedBuilder getBuilder(String title) {    
        return new EmbedBuilder()
                .withAuthorIcon(Main.getClient().getOurUser().getAvatarURL())
                .withAuthorName(title);
    }
    
    public static String buildUsage(String alias, String args, String desc) {
        return (String.format("!%s %s \n\n%s", alias, args, desc));
    }
    
}
