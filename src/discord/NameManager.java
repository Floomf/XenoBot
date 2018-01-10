package discord;

import discord.objects.User;
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
            BotUtils.setNickname(guild, dUser, name);
        }
    }
    
}
