package discord;

import discord.objects.Rank;
import discord.objects.User;
import java.awt.Color;
import java.util.List;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;

public class RankManager {
    
    //hardcoded ranks
    private final static Rank[] RANKS = {
        new Rank(386829087139692556L, "Peasant", 0),
        new Rank(386829020161114112L, "Citizen", 10),
        new Rank(275486798699036683L, "Knight", 20),
        new Rank(386829239409704961L, "Hero", 30),
        new Rank(387908458382491650L, "Lord", 40),
        new Rank(387908362450370560L, "King", 50),
        new Rank(342028348886614016L, "Ancient", 60),
        new Rank(387905623662133249L, "Titan", 70),
        new Rank(386935240729296899L, "God", 80)
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
            for (Rank rank : RANKS) {
                IRole role = guild.getRoleByID(rank.getID());
                if (guildRoles.contains(role)) {
                    guildRoles.remove(role);
                    break;
                }
            }
            guildRoles.add(guild.getRoleByID(user.getRank().getID()));
            BotUtils.setRoles(guild, dUser, guildRoles.toArray(new IRole[guildRoles.size()]));
            System.out.println("Set role of " + user.getName() + " to " + user.getRank().getName());
            if (!rankNeeded.equals(RANKS[0])) {
                BotUtils.sendMessage(guild.getChannelsByName("log").get(0),
                        BotUtils.getMention(user), "Rank up!",
                        "You have been promoted to (a/an) **" + rankNeeded.getName() + "**.",
                        Color.MAGENTA);
            }
        }
    }
    
}
