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
        new Rank("Coconut", 0),
        new Rank("Apple", 10),
        new Rank("Grape", 20),
        new Rank("Lemon", 30),
        new Rank("Strawberry", 40),
        new Rank("Peach", 50),
        new Rank("Kiwi", 60),
        new Rank("Watermelon", 70),
        new Rank("Pineapple", 80)
    };
    
    public static Rank getRankForLevel(int level) {
        for (Rank rank : RANKS) {
            if (level - rank.getLevelRequired() <= 9)
                return rank;
        }
        return RANKS[RANKS.length - 1]; //last rank
    }
    
    public static void setRankOfUser(IGuild guild, User user) {
        Rank rankNeeded = getRankForLevel(user.getProgress().getLevel());
        IRole rankRole = guild.getRolesByName(rankNeeded.getName()).get(0);
        IUser dUser = guild.getUserByID(user.getID());
        List<IRole> guildRoles = dUser.getRolesForGuild(guild);
        //if rank isnt what it should be, set it correctly so
        if (!rankNeeded.equals(user.getProgress().getRank()) || !guildRoles.contains(rankRole)) {
            user.getProgress().setRank(rankNeeded);
            for (Rank rank : RANKS) { //remove all existing rank roles
                IRole role = guild.getRolesByName(rank.getName()).get(0);
                if (guildRoles.contains(role)) {
                    guildRoles.remove(role);
                    break;
                }
            }
            guildRoles.add(rankRole);
            BotUtils.setUserRoles(guild, dUser, guildRoles);
            System.out.println("Set role of " + user.getName() + " to " + 
                    user.getProgress().getRank().getName());
            if (!rankNeeded.equals(RANKS[0])) {
                BotUtils.sendMessage(guild.getChannelsByName("log").get(0),
                        BotUtils.getMention(user), "Rank up!",
                        "You are now a **" + rankNeeded.getName() + "**.",
                        rankRole.getColor());
            }
        }
    }
    
}
