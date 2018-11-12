package discord;

import discord.object.Rank;
import discord.object.User;
import java.util.List;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;

public class RankManager {
    
    //hardcoded ranks
    private final static Rank[] RANKS = {
        new Rank("Fallen", "The Fallen", 0),
        new Rank("Ordinary", "The Ordinary", 10),
        new Rank("Peculiar", "The Peculiar", 20),
        new Rank("Brave", "The Brave", 30),
        new Rank("Honored", "The Honored", 40),
        new Rank("Mighty", "The Mighty", 50),
        new Rank("Resolute", "The Resolute", 60),
        new Rank("Victorious", "The Victorious", 70),
        new Rank("Divine", "The Divine", 80)
    };
    
    public static Rank getRankForLevel(int level) {       
        for (int i = 0; i < RANKS.length; i++) {
            if (level < RANKS[i].getLevelRequired()) {
                return RANKS[i-1];
            } 
        }
        return RANKS[RANKS.length - 1]; //has to be last rank
    }
    
    public static void setRankOfUser(IGuild guild, User user) {
        Rank rankNeeded = getRankForLevel(user.getProgress().getLevel());
        
        if (!rankNeeded.equals(user.getProgress().getRank())) {
            user.getProgress().setRank(rankNeeded);
            verifyRankOnGuild(guild, user);
            if (!rankNeeded.equals(RANKS[0])) {
               BotUtils.sendMessage(guild.getChannelsByName("log").get(0),
                        BotUtils.getMention(user), "Rank up!",
                        "You are now **" + rankNeeded.getName() + "**.",
                        guild.getRolesByName(rankNeeded.getRoleName()).get(0).getColor());
            }
        }
    }
    
    public static void verifyRankOnGuild(IGuild guild, User user) {
         IRole rankRole = guild.getRolesByName(user.getProgress().getRank().getRoleName()).get(0);
         IUser dUser = guild.getUserByID(user.getDiscordID());
         List<IRole> guildRoles = dUser.getRolesForGuild(guild);
         if (!guildRoles.contains(rankRole)) {
            for (Rank rank : RANKS) { //remove all existing rank roles
                IRole role = guild.getRolesByName(rank.getRoleName()).get(0);
                if (guildRoles.contains(role)) {
                    guildRoles.remove(role);
                }
            }
            guildRoles.add(rankRole);
            BotUtils.setUserRoles(guild, dUser, guildRoles);
            System.out.println("Set role of " + user.getName() + " to " + 
                    user.getProgress().getRank().getRoleName());
        }
    }
    
}
