package discord;

import discord.objects.User;
import java.awt.Color;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

public class LevelManager {
    
    public static final int MAX_LEVEL = 80;
    
    public static void addUserXPFromID(IGuild guild, long id, int amount) {
        User user = UserManager.getUserFromID(id);
        user.addXP(amount); 
        checkXPUser(guild, user);
    }   
    
    //this needs to be a lot cleaner, fix it sooner
    private static void changeLevelUser(IGuild guild, User user, boolean levelUp) {
        int amount = (levelUp) ? 1 : -1;
        
        if (levelUp) {          
            user.addXP(-user.getXPForLevel()); //carry over xp to next level
        }  
        
        user.addLevels(amount);
        setUserXPForLevel(user);     
        
        if (!levelUp) {           
            user.addXP(user.getXPForLevel()); //subtract from the max
        }
        
        NameManager.formatNameOfUser(guild, user);         
        int level = user.getLevel();
        System.out.println(user.getName() + " is now level " + level);
        BotUtils.sendMessage(guild.getChannelsByName("log").get(0), "Level up!",
                String.format("%s\n**%d → %d**", BotUtils.getMention(user), 
                        level - 1, level), Color.ORANGE); 
        
        RankManager.setRankOfUser(guild, user);
        
        if (user.getLevel() == MAX_LEVEL) 
            maxOutUser(guild.getChannelsByName("log").get(0), user);
        else 
            checkXPUser(guild, user);
    }
    
    private static void maxOutUser(IChannel channel, User user) {
        user.setXPForLevel(0);
        user.setXP(0);
        BotUtils.sendMessage(channel, "Congratulations!", "You have reached the max level. "
                + "*You will no longer earn **any xp** until prestiged.*"
                + "\n\nYou can now prestige and carry over back to level one with `!prestige`"
                + "\n\nYou will keep all level perks, and gain additional customization perks."
                + "\n\nPrestiging is **PERMANENT.** Only do so if you are ready.");
    }
    
    public static void prestigeUser(IChannel channel, User user) {
        user.addPrestige();
        user.setLevel(1);
        user.setXP(0);
        setUserXPForLevel(user);
        IGuild guild = channel.getGuild();
        NameManager.formatNameOfUser(guild, user);
        RankManager.setRankOfUser(guild, user);
        BotUtils.sendMessage(channel, "@here", "Alert", 
                user.getName() + " has **prestiged!** Praise unto thee.", Color.CYAN);
    }
    
    public static void checkXPUser(IGuild guild, User user) {
        int xp = user.getXP();
        if (xp >= user.getXPForLevel())
            changeLevelUser(guild, user, true);
        else if (xp < 0)
            changeLevelUser(guild, user, false);
    }
    
    public static void setUserXPForLevel(User user) {
        user.setXPForLevel(user.getLevel() * 10 + 50);       
    }
    
    public static EmbedObject buildInfo(User user, IUser dUser) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.withColor(Color.CYAN);
        builder.withAuthorIcon(dUser.getAvatarURL());
        builder.withAuthorName(user.getName());
        builder.withDesc(user.getRank().getName());
        builder.appendField("Level", "`" + user.getLevel() + "`", true);
        int prestige = user.getPrestige();
        if (prestige > 0) 
            builder.appendField("Prestige", String.format("`%d%c`", 
                    prestige, BotUtils.PRESTIGE_SYMBOLS[prestige - 1]), true);
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
      
}