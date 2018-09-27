package discord.object;

import com.fasterxml.jackson.annotation.JsonIgnore;
import discord.BotUtils;
import discord.LevelManager;
import discord.NameManager;
import discord.RankManager;
import sx.blah.discord.handle.obj.IGuild;
import java.awt.Color;
import sx.blah.discord.handle.obj.IChannel;

public class Progress {
    
    private int level;
    private int xp;
    private int xpTotalForLevelUp;
    private Prestige prestige;
    private Rank rank;
    
    public final static int MAX_LEVEL = 80;
    
    public Progress(int level, int xp, int xpTotalForLevelUp, int prestige, Rank rank) {
        this.level = level;
        this.xp = xp;
        this.xpTotalForLevelUp = xpTotalForLevelUp;
        this.prestige = new Prestige(prestige);
        this.rank = rank;
    }
    
    protected Progress() {
        level = 1;
        xp = 0;
        genXPTotalForLevelUp();
        prestige = new Prestige(0);
        rank = RankManager.getRankForLevel(level);
    }
    
    //getters
    
    public int getLevel() {
        return level;
    }
    
    public int getXP() {
        return xp;
    }
    
    public int getXpTotalForLevelUp() {
        return xpTotalForLevelUp;
    }
    
    public Prestige getPrestige() {
        return prestige;
    }
    
    public Rank getRank() {
        return rank;
    }
    
    //TEMPORARY
    public void setRank(Rank rank) {
        this.rank = rank;
    }
    
    @JsonIgnore
    public int getTotalLevels() {
        return prestige.getNumber() * 80 + level;
    }
    
    @JsonIgnore
    public boolean isMaxLevel() {
        return level == MAX_LEVEL;
    }
    
    protected void addXP(int xp, IGuild guild, User user) {
        if (level < MAX_LEVEL) {
            this.xp += xp;
            checkXP(guild, user);
        }
    }
    
    private void checkXP(IGuild guild, User user) {
        if (xp >= xpTotalForLevelUp) {
            levelUp(guild, user);
            checkUnlocksForUser(guild, user);
            RankManager.setRankOfUser(guild, user);
            if (level < MAX_LEVEL) checkXP(guild, user);
        } else if (xp < 0) {
            levelDown(guild, user);
            RankManager.setRankOfUser(guild, user);
            if (level < MAX_LEVEL) checkXP(guild, user);
        }
    }
    
    //Only handles leveling up, logic and xp handling handled elsewhere
    private void levelUp(IGuild guild, User user) {
        xp -= xpTotalForLevelUp; //carry over xp to next level by subtracting from level xp
        level++;
        genXPTotalForLevelUp();
        BotUtils.sendMessage(guild.getChannelsByName("log").get(0), "Level up!",
                String.format("%s\n**%d → %d**", BotUtils.getMention(user), 
                        level - 1, level), Color.ORANGE);
    }
    
    //same as leveling up method
    private void levelDown(IGuild guild, User user) {
        level--;
        genXPTotalForLevelUp();
        xp += xpTotalForLevelUp; //add negative xp to new level xp
        BotUtils.sendMessage(guild.getChannelsByName("log").get(0), "Level down!",
                String.format("%s\n**%d → %d**", BotUtils.getMention(user), 
                        level, level - 1), Color.RED);
    }
    
    private void checkUnlocksForUser(IGuild guild, User user) {
        if (level % 20 == 0) {
            IChannel pmChannel = guild.getClient().getOrCreatePMChannel(guild.getUserByID(user.getID()));
            LevelManager.notifyUnlocksForUser(pmChannel, user);
            if (level == MAX_LEVEL) {
                //send DM to user
                maxOutUser(pmChannel, user);
            }
        }
    }
    
    private void maxOutUser(IChannel channel, User user) {
        xp = 0;
        xpTotalForLevelUp = 0;
        BotUtils.sendMessage(channel, "Congratulations!", "You have reached the max level. "
                + "*You will no longer earn **any more xp** until prestiged.*"
                + "\n\nYou can now prestige and carry over back to level one with `!prestige`"
                + "\n\nYou will keep all level perks, and gain additional unlocks as you level again."
                + "\n\nPrestiging is **PERMANENT.** Only do so if you are ready.", Color.CYAN);
    }
    
    //BAD
    protected void prestige(IGuild guild, User user) {
        prestige = prestige.prestige();
        level = 1;
        xp = 0;
        genXPTotalForLevelUp();
        RankManager.setRankOfUser(guild, user);
        NameManager.formatNameOfUser(guild, user);
        BotUtils.sendMessage(guild.getChannelsByName("log").get(0), "@here", "ALERT", 
                user.getName() + " is now **Prestige " + prestige.getNumber()
                        + "!** Praise unto thee.", Color.CYAN);
        //bad to put it here, organize unlock messages in future
        if (prestige.getNumber() == 1) {
            BotUtils.sendMessage(guild.getClient().getOrCreatePMChannel(guild.getUserByID(user.getID())), 
                    "Congratulations!", "You have unlocked the ability to **change your name color** on The Realm!"
                        + "\n\n*You can type* `!color` *on the server get started.*");
        }
    }
    
    private void genXPTotalForLevelUp() {
        xpTotalForLevelUp = level * 10 + 50;       
    } 
    
}
