package discord;

import discord.objects.User;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.util.EmbedBuilder;

public class LevelManager {
    
    public static void addUserXPFromID(IGuild guild, long id, int amount) {
        User user = UserManager.getUserFromID(id);
        user.addXP(amount); 
        checkXPUser(guild, user);
    }   
    
    //this needs to be a lot cleaner, fix it sooner
    private static void changeLevelUser(IGuild guild, User user, boolean levelUp) {
        IChannel botsChannel = guild.getChannelByID(250084663618568192L);
        int amount = (levelUp) ? 1 : -1;
        
        if (levelUp) {          
            user.addXP(-user.getXPForLevel()); //carry over xp to next level
        }       
        
        user.addLevels(amount);
        setUserXPForLevel(user);     
        
        if (!levelUp) {           
            user.addXP(user.getXPForLevel() + user.getXP()); //subtract from the max
        }
        
        NameManager.formatNameOfUser(guild, user);                     
        BotUtils.sendMessage(botsChannel, guild.getUserByID(user.getID()).mention() 
                + "```Level up! You are now level " + user.getLevel() + ".```"); 
        
        RankManager.setRankOfUser(guild, user);
        
        if (user.getLevel() == BotUtils.MAX_LEVEL) 
            maxOutUser(botsChannel, user);
        else 
            checkXPUser(guild, user);
    }
    
    private static void maxOutUser(IChannel channel, User user) {
        user.setXPForLevel(0);
        user.addXP(-user.getXP());
        BotUtils.sendMessage(channel, "```Congratulations! You have reached the max level."
                + "\n\nYou can now prestige and carry over back to level one with \"!prestige\"! "
                + "\n\nYour prestige will show with your name. Level perks need to be unlocked again to use them. "
                + "Prestiging is permanent. Only do so if you are ready.```");
    }
    
    private static void checkXPUser(IGuild guild, User user) {
        int xp = user.getXP();
        if (xp >= user.getXPForLevel())
            changeLevelUser(guild, user, true);
        else if (xp < 0)
            changeLevelUser(guild, user, false);
    }
    
    public static void setUserXPForLevel(User user) {
        user.setXPForLevel(user.getLevel() * 30 + 70);
    }
    
    public static EmbedObject buildInfo(User user) {
        EmbedBuilder builder = new EmbedBuilder();
        
        builder.withColor(0, 255, 127);
        builder.withTitle("__" + user.getName() + "__");
        builder.withDesc(user.getRank().getName());
        builder.appendField("Level", "`" + user.getLevel() + "`", true);
        builder.appendField("XP", "`" + user.getXP() + "/" + user.getXPForLevel() + "`", true);
        builder.appendField("Progress to Next Level", getProgress(user), false);
        
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
    /* Used for changing xp system  
    public static void fixUserLevels(IGuild guild, User user) {
        for (int i = 1; i < user.getLevel(); i++) {
            user.addXP(i * 10 + 15);
            checkXPUser(guild, user);    
        }
    }*/
   
}
