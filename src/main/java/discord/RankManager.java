package discord;

import com.fasterxml.jackson.databind.ObjectMapper;
import discord.object.Rank;
import discord.object.User;
import java.io.File;
import java.io.IOException;
import java.util.List;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;

public class RankManager {   
    
    private static Rank[] RANKS;
    
    static {
        ObjectMapper mapper = new ObjectMapper();
        try {
            RANKS = mapper.readValue(new File("ranks.json"), Rank[].class);
            System.out.println(RANKS.length + " ranks loaded.");
        } catch (IOException e) {
            System.out.print("Could not load ranks with error: " + e);
        }
    }
    
    public static Rank getRankForLevel(int level) {       
        for (int i = 0; i < RANKS.length; i++) {
            if (level < RANKS[i].getLevelRequired()) {
                return RANKS[i-1];
            } 
        }
        return RANKS[RANKS.length - 1]; //has to be last rank
    }
    
    public static void verifyRankOfUser(IGuild guild, User user) {
        Rank rankNeeded = getRankForLevel(user.getProgress().getLevel());        
        if (!rankNeeded.equals(user.getProgress().getRank())) {
            user.getProgress().setRank(rankNeeded);
            verifyRoleOnGuild(guild, user);
            if (!rankNeeded.equals(RANKS[0])) {
               BotUtils.sendMessage(guild.getChannelsByName("log").get(0),
                        BotUtils.getMention(user), "Rank up!",
                        "You are now **" + rankNeeded.getName() + "**.",
                        guild.getRolesByName(rankNeeded.getRoleName()).get(0).getColor());
            }
        }
    }
    
    public static void verifyRoleOnGuild(IGuild guild, User user) {
         List<IRole> roles = guild.getRolesByName(user.getProgress().getRank().getRoleName());
         if (roles.isEmpty()) {
             System.out.println("COULD NOT FIND ROLE `" + user.getProgress().getRank().getRoleName()
                     + "` FOR RANK `" + user.getProgress().getRank().getName() + "` ON GUILD! PLEASE CREATE ONE.");
             return;
         }
         
         IRole rankRole = roles.get(0);
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
