package discord;

import java.util.List;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

public class NameManager {
    
    public static void formatNameOfUser(IGuild guild, User user) {        
        String name = String.format("%s [%d]", user.getName(), user.getLevel());
        IUser dUser = guild.getUserByID(user.getID());
        String nick = dUser.getNicknameForGuild(guild);
        if (nick == null || !nick.equals(name)) {
            if (guild.getOwner().getLongID() == user.getID()) {
                System.out.println("Can't set owner's nickname");
                return;
            }     
            BotUtils.setName(guild, dUser, name);
        }
    }
    
    public static void formatAllNames(IGuild guild, List<User> users) {
        System.out.println("Formatting all names...");
        for (User user : users) {
            formatNameOfUser(guild, user);
        }
        //System.out.println("Finished formatting all names.");
        //fix this
    }
    
}
