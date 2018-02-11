package discord;

import discord.objects.User;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

public class NameManager {
    
    public static void formatNameOfUser(IGuild guild, User user) {        
        String name = buildName(user.getName(), user.getEmoji());
        IUser dUser = guild.getUserByID(user.getID());
        String nick = dUser.getNicknameForGuild(guild);
        if (nick == null || !nick.equals(name)) {
            if (guild.getOwner().getLongID() == user.getID()) {
                System.out.println("Need to set owner's name to " + name);
                return;
            }     
            BotUtils.setNickname(guild, dUser, name);
        }
    }
    
    private static String buildName(String name, int emojicp) {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(" ");
        if (emojicp > 0) {
            sb.appendCodePoint(emojicp).append(" ");
        }
        return sb.toString();
    }
    
}
