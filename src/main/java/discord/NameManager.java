package discord;

import discord.objects.User;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

public class NameManager {
    
    public static void formatNameOfUser(IGuild guild, User user) {        
        String name = buildName(user.getName(), user.getEmoji(), user.getPrestige());
        IUser dUser = guild.getUserByID(user.getID());
        String nick = dUser.getNicknameForGuild(guild);
        if ((nick == null && !dUser.getName().equals(name)) 
                || (nick != null && !nick.equals(name))) {
            if (guild.getOwner().getLongID() == user.getID()) {
                System.out.println("Need to set owner's name to " + name);
                return;
            }
            BotUtils.setNickname(guild, dUser, name);
        }
    }
    
    public static void setNameOfUser(IGuild guild, User user, String name) {
        user.setName(name);
        formatNameOfUser(guild, user);        
    }
    
    private static String buildName(String name, int emojicp, int prestige) {
        StringBuilder sb = new StringBuilder();
        if (prestige > 0) {
            sb.append(BotUtils.PRESTIGE_SYMBOLS[prestige - 1]).append(" ");
        }
        sb.append(name);
        if (emojicp > 0) {
            sb.append(" ").appendCodePoint(emojicp);
        }
        return sb.toString();
    }
    
}
