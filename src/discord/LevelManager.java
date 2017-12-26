package discord;

import discord.objects.Rank;
import discord.objects.User;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.util.EmbedBuilder;

public class LevelManager {
    
    public static void addUserXP(IGuild guild, long id, int amount) {
        User user = UserManager.getUserFromID(id);
        user.addXP(amount); 
        if (checkLevelupUser(user))
            addLevelsUser(guild, user, 1);
        else if (checkLeveldownUser(user))
            addLevelsUser(guild, user, -1);
    }
    
    //this needs to be a lot cleaner, fix it sooner
    private static void addLevelsUser(IGuild guild, User user, int amount) {
        IChannel botsChannel = guild.getChannelByID(250084663618568192L);
        
        if (amount > 0) {
            user.addXP(-user.getXPForLevel()); //carry over xp to next level
        }
        
        user.addLevels(amount); 
        user.setXPForLevel(genXPForLevel(user.getLevel()));
        
        if (amount < 0) {
            user.addXP(user.getXPForLevel() + user.getXP()); //subtract from the max
        }
        
        NameManager.formatNameOfUser(guild, user);
     
        BotUtils.sendMessage(botsChannel, guild.getUserByID(user.getID()).mention() 
                + "```Level up! You are now level " + user.getLevel() + ".```"); 
        
        Rank rankNeeded = RankManager.getRankForLevel(user.getLevel());
        if (!user.getRank().equals(rankNeeded)) {         
            user.setRank(rankNeeded);
            RankManager.setRankOfUser(guild, user);
            BotUtils.sendMessage(botsChannel,
                    "```Congratulations! You are now (a/an) " + rankNeeded.getName() + ".```");
        }
        
        if (checkLevelupUser(user)) 
            addLevelsUser(guild, user, 1);
        if (checkLeveldownUser(user))
            addLevelsUser(guild, user, -1);
    }
    
    private static boolean checkLevelupUser(User user) {
        return (user.getXP() >= user.getXPForLevel());
    }
    
    private static boolean checkLeveldownUser(User user) {
        return (user.getXP() < 0);
    }
    
    public static int genXPForLevel(int level) {
        return BotUtils.XP_MULTIPLIER * (level * 40 + 50);
    }
    
    public static EmbedObject buildInfo(User user) {
        EmbedBuilder builder = new EmbedBuilder();
        
        builder.withColor(0, 255, 127);
        builder.withTitle("__" + user.getName() + "__");
        builder.withDesc(user.getRank().getName());
        builder.appendField("Level", "`" + user.getLevel() + "`", true);
        builder.appendField("XP", "`" + user.getXP() + "/"+ user.getXPForLevel() + "`", true);
        builder.appendField("Progress", getProgress(user), false);
        
        return builder.build();
    }
    
    private static String getProgress(User user) {
        StringBuilder builder = new StringBuilder();
        //generates an int 1-10 depicting progress based on xp
        int prog = (int) Math.floor(((double) user.getXP() / user.getXPForLevel()) * 10);
        
        for (int i = 1; i <= 10; i++) {
            if (i <= prog)
                builder.append(":white_large_square: ");
            else
                builder.append(":white_square_button: ");
        }
        return builder.toString();
    }
    /*   
    public static void fixUserLevels(IGuild guild, User user) {
        user.addXP(user.getLevel() * -25);
        if (checkLeveldownUser(user))
            addLevelsUser(guild, user, -1);       
    }*/
   
}
