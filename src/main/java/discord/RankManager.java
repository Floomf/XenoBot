package discord;

import discord.object.Rank;
import discord.object.User;
import java.awt.Color;
import java.util.List;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;

public class RankManager {
    
    //hardcoded ranks
    private final static Rank[] RANKS = {
        new Rank(386829087139692556L, "Coconut", 0),
        new Rank(386829020161114112L, "Apple", 10),
        new Rank(275486798699036683L, "Grape", 20),
        new Rank(386829239409704961L, "Lemon", 30),
        new Rank(387908458382491650L, "Strawberry", 40),
        new Rank(387908362450370560L, "Peach", 50),
        new Rank(342028348886614016L, "Kiwi", 60),
        new Rank(387905623662133249L, "Watermelon", 70),
        new Rank(386935240729296899L, "Pineapple", 80)
    };
    
    public static Rank getRankForLevel(int level) {
        for (Rank rank : RANKS) {
            if (level - rank.getLevelRequired() <= 9)
                return rank;
        }
        return RANKS[RANKS.length - 1]; //last rank
    }
    
    public static void setRankOfUser(IGuild guild, User user) {
        Rank rankNeeded = getRankForLevel(user.getLevel());
        IUser dUser = guild.getUserByID(user.getID());
        List<IRole> guildRoles = dUser.getRolesForGuild(guild);
        //if rank isnt what it should be, set it correctly so
        if (!rankNeeded.equals(user.getRank())
                || !guildRoles.contains(guild.getRoleByID(user.getRank().getID()))) {
            user.setRank(rankNeeded);
            for (Rank rank : RANKS) { //remove all existing rank roles
                IRole role = guild.getRoleByID(rank.getID());
                if (guildRoles.contains(role)) {
                    guildRoles.remove(role);
                    break;
                }
            }
            IRole rankRole = guild.getRoleByID(rankNeeded.getID());
            guildRoles.add(rankRole);
            BotUtils.setUserRoles(guild, dUser, guildRoles);
            System.out.println("Set role of " + user.getName() + " to " + user.getRank().getName());
            if (!rankNeeded.equals(RANKS[0])) {
                BotUtils.sendMessage(guild.getChannelsByName("log").get(0),
                        BotUtils.getMention(user), "Rank up!",
                        "You are now a **" + rankNeeded.getName() + "**.",
                        rankRole.getColor());
            }
        }
    }
    
}
