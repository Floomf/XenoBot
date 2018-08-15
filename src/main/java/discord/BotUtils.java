package discord;

import discord.objects.User;
import java.awt.Color;
import java.util.List;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.RequestBuffer;

public class BotUtils {  
  
    public static final String CMD_PREFIX = "!";
    
    public static void sendEmbedMessage(IChannel channel, EmbedObject object) {
        RequestBuffer.request(() -> {           
            try{
                channel.sendMessage(object);
            } catch (DiscordException e){
                System.err.println("Message could not be sent with error: " + e);
            }
        }).get();
    }
    
    public static void sendMessage(IChannel channel, String outside, String header, String body, Color color) {
        RequestBuffer.request(() -> {
            try {
                channel.sendMessage(outside, getBuilder(header, body, color).build());
            } catch (DiscordException e){
                System.err.println("Message could not be sent with error: " + e);
            }
        }).get(); //.get() makes sure they send in order cause async??
    }
    
    public static void sendMessage(IChannel channel, String header, String body, Color color) {
        sendMessage(channel, "", header, body, color);
    }  
    
    public static void sendMessage(IChannel channel, String header, String body) {
        sendMessage(channel, header, body, Color.WHITE);
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
    
    public static void setUserNickname(IGuild guild, IUser user, String name) {
        RequestBuffer.request(() -> {
            try{
                guild.setUserNickname(user, name);
                System.out.println("Set nickname of " + user.getName() + " to " + name);
            } catch (DiscordException e){
                System.err.println("Name could not be set with error: " + e);
            }
        });
    }
    
    public static void setUserRoles(IGuild guild, IUser user, List<IRole> roles) {
        RequestBuffer.request(() -> {
            try{
                guild.editUserRoles(user, roles.toArray(new IRole[roles.size()]));
            } catch (DiscordException e) {
                System.err.println("Roles could not be set with error: " + e);
            }
        });
    }   
    
    public static EmbedBuilder getBuilder(String title, String desc, Color color) {    
        return new EmbedBuilder()
                .withAuthorIcon(Main.getClient().getOurUser().getAvatarURL())
                .withAuthorName(title)
                .withDesc(desc)
                .withColor(color);              
    }
    
    public static EmbedBuilder getBuilder(String title, String desc) {
        return getBuilder(title, desc, Color.WHITE);
    }
    
    public static String getMention(User user) {
        return "<@!" + user.getID() + ">";
    }
    
    public static String buildUsage(String alias, String args, String desc) {
        return (String.format("%s%s %s \n\n%s", CMD_PREFIX, alias, args, desc));
    }
    
}
