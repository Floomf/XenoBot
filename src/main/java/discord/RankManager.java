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
        new Rank("Skeleton", "Spooky Skeletons", 0),
        new Rank("Goblin", "The Goblin Gang", 10),
        new Rank("Witch", "The Witch Coven", 20),
        new Rank("Zombie", "The Zombie Horde", 30),
        new Rank("Pirate", "The Pirate Crew", 40),
        new Rank("Vampire", "The Vampire Bloodline", 50),
        new Rank("Werewolf", "The Werewolf Pack", 60),
        new Rank("Demon", "The Demon Legion", 70),
        new Rank("Death", "Death", 80)
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
                BotUtils.sendMessage(guild.getChannelsByName("securitycam").get(0),
                        BotUtils.getMention(user), "Rank up!",
                        "You are now a **" + rankNeeded.getName() + "**.",
                        guild.getRolesByName(rankNeeded.getName()).get(0).getColor());
            }
        }
    }
    
    public static void verifyRankOnGuild(IGuild guild, User user) {
         IRole rankRole = guild.getRolesByName(user.getProgress().getRank().getRoleName()).get(0);
         IUser dUser = guild.getUserByID(user.getID());
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
