package discord;

import discord.objects.Rank;
import discord.objects.User;
import java.util.List;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;

public class RankManager {
    
    private final static Rank[] RANKS = {
        new Rank(386829087139692556L, "Guest", 0),
        new Rank(386829020161114112L, "Casual", 5),
        new Rank(275486798699036683L, "Regular", 10),
        new Rank(387905365565767681L, "Follower", 15),
        new Rank(386828818838454272L, "Dedicated", 20),
        new Rank(386829239409704961L, "Addict", 25),
        new Rank(386829752545312768L, "Noble", 30),
        new Rank(387908458382491650L, "Hero", 35),
        new Rank(386829341209919489L, "Elder", 40),
        new Rank(342028348886614016L, "Ancient", 45),
        new Rank(387908362450370560L, "Legend", 50),
        new Rank(387905623662133249L, "Mythic", 55),
        new Rank(386935240729296899L, "God", 60)
    };
    
    public static Rank getRankForLevel(int level) {
        for (Rank rank : RANKS) {
            if (level - rank.getLevelRequired() <= 4)
                return rank;
        }
        return RANKS[RANKS.length - 1]; //last rank
    }
    
    public static void setRankOfUser(IGuild guild, User user) {        
        IUser dUser = guild.getUserByID(user.getID());
        List<IRole> guildRoles = dUser.getRolesForGuild(guild);
        if (!guildRoles.contains(guild.getRoleByID(user.getRank().getID()))) {
            //remove only existing "rank" roles on the user, this way other roles are kept
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
        }
    }
    
    public static void setRanksForGuild(IGuild guild, List<User> users) {
        for (User user : users) {
            setRankOfUser(guild, user);
        }
    }
    
}
