package discord.data.object;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import discord.util.BotUtils;
import discord.data.ColorManager;
import discord.data.RankManager;
import sx.blah.discord.handle.obj.IGuild;
import java.awt.Color;
import sx.blah.discord.handle.obj.IChannel;

public class Progress {

    public final static int MAX_LEVEL = 80;
    
    public static double GLOBAL_XP_MULTIPLIER = 1.0;
    
    private final static int XP_SCALE = 10;
    private final static int XP_FLAT = 50;

    @JsonIgnore
    private User user;

    private int level;
    private double xp;
    private int xpTotalForLevelUp;
    @JsonIgnore
    private Rank rank;
    private Prestige prestige;
    private Reincarnation reincarnation;

    @JsonCreator
    protected Progress(@JsonProperty("level") int level,
            @JsonProperty("xp") int xp,
            @JsonProperty("xpTotalForLevelUp") int xpTotalForLevelUp,
            @JsonProperty("prestige") Prestige prestige,
            @JsonProperty("reincarnation") Reincarnation reincarnation) {
        this.level = level;
        this.xp = xp;
        this.xpTotalForLevelUp = xpTotalForLevelUp;
        this.prestige = prestige;
        this.reincarnation = reincarnation;
        this.rank = prestige.isMax()
                ? RankManager.getRankForLevel(MAX_LEVEL)
                : RankManager.getRankForLevel(level);
    }

    protected Progress() {
        genDefaultStats();
        reincarnation = new Reincarnation(0);
    }

    public int getLevel() {
        return level;
    }

    public double getXP() {
        return xp;
    }

    public int getXpTotalForLevelUp() {
        return xpTotalForLevelUp;
    }

    public Rank getRank() {
        return rank;
    }

    public Prestige getPrestige() {
        return prestige;
    }

    public Reincarnation getReincarnation() {
        return reincarnation;
    }

    @JsonIgnore
    public double getXPMultiplier() {
        return 0.5 * reincarnation.getNumber() + 1.0;
    }

    @JsonIgnore
    public int getTotalLevelThisLife() {
        return prestige.getNumber() * MAX_LEVEL + level;
    }
    
    @JsonIgnore
    public int getTotalLevel() {
        return getTotalLevelThisLife() + reincarnation.getNumber() * (MAX_LEVEL * Prestige.MAX_PRESTIGE);
    }
    
    @JsonIgnore
    public int getTotalXP() {
        int totalXP = 0;
        totalXP += reincarnation.getNumber() * getTotalXPToPrestige(Prestige.MAX_PRESTIGE);
        totalXP += getTotalXPToPrestige(prestige.getNumber());
        totalXP += getTotalXPToLevel(level);
        totalXP += xp;
        return totalXP;
    }
                                    
    @JsonIgnore 
    public double getXPRate(int users) {
        return 0.5 * (GLOBAL_XP_MULTIPLIER + getXPMultiplier() - 1) * (users + 13); //Maybe can simplify this?
    }
    
    @JsonIgnore
    private static int getTotalXPToPrestige(int prestige) {
        int totalXP = 0;
        for (int i = 0; i < prestige; i++) {
            totalXP += getTotalXPToLevel(Progress.MAX_LEVEL);
        }
        return totalXP;
    }
    
    @JsonIgnore
    public static int getTotalXPToLevel(int level) {
        int totalXP = 0;
        for (int i = 1; i < level; i++) {
            totalXP += i * XP_SCALE + XP_FLAT; //hardcoded
        }
        return totalXP;
    }

    @JsonIgnore
    public boolean isMaxLevel() {
        return (level == MAX_LEVEL && !prestige.isMax()); //Max prestige levels infinitely
    }

    @JsonIgnore
    public boolean isNotMaxLevel() {
        return !isMaxLevel();
    }

    //TEMPORARY?
    @JsonIgnore
    public void setUser(User user) {
        this.user = user;
    }

    @JsonIgnore
    public void setRank(Rank rank) {
        this.rank = rank;
    }

    public void addXP(double xp, IGuild guild) {
        if (isNotMaxLevel()) {
            this.xp += xp;
            checkXP(guild);
        }
    }
    
    public void addPeriodicXP(int users, IGuild guild) {
        addXP(getXPRate(users), guild);
    }

    private void checkXP(IGuild guild) {
        if (xp >= xpTotalForLevelUp || xp < 0) {
            if (xp >= xpTotalForLevelUp) {
                levelUp(guild);
                checkUnlocksForUser(guild);
            } else if (xp < 0) {
                if (level > 1) { //prevent negative levels
                    levelDown(guild);
                } else {
                    xp = 0;
                }
            }
            if (!prestige.isMax()) {//keep last rank at max prestige
                RankManager.verifyRankOfUser(guild, user);
            }
            if (isNotMaxLevel()) {
                checkXP(guild);
            }
        }
    }

    //Only handles leveling up, logic and xp handling handled elsewhere
    private void levelUp(IGuild guild) {
        xp -= xpTotalForLevelUp; //carry over xp to next level by subtracting from level xp
        level++;
        genXPTotalForLevelUp();
        BotUtils.sendMessage(guild.getChannelsByName("log").get(0), "Level up!",
                String.format("%s\n**%d → %d**", BotUtils.getMention(user),
                        level - 1, level), guild.getUserByID(user.getDiscordID()).getColorForGuild(guild));
    }

    //same as leveling up method
    private void levelDown(IGuild guild) {
        level--;
        genXPTotalForLevelUp();
        xp += xpTotalForLevelUp; //add negative xp to new level xp
        BotUtils.sendMessage(guild.getChannelsByName("log").get(0), "Level down!",
                String.format("%s\n**%d → %d**", BotUtils.getMention(user),
                        level, level - 1), guild.getUserByID(user.getDiscordID()).getColorForGuild(guild));
    }

    private void checkUnlocksForUser(IGuild guild) {
        IChannel pmChannel = guild.getClient().getOrCreatePMChannel(guild.getUserByID(user.getDiscordID()));
        notifyPossibleUnlocks(pmChannel, guild);
        if (isMaxLevel()) {
            maxOut(guild, pmChannel);
        }
    }

    private void maxOut(IGuild guild, IChannel pmChannel) {
        xp = 0;
        xpTotalForLevelUp = 0;
        
        if (user.getPrefs().get(Pref.AUTO_PRESTIGE)) {
            RankManager.verifyRankOfUser(guild, user); //have to put this here, this whole design is fucked
            prestige(guild);
            return;
        }
        
        if (getTotalLevelThisLife() == MAX_LEVEL * Prestige.MAX_PRESTIGE) {
            BotUtils.sendMessage(pmChannel, "Incredible!", "You have reached the max level for the final time, "
                    + "and may now move onto the maximum prestige with `!prestige`."
                    + "\n\nAs always, prestiging is **permanent.** Only do so if you are ready.", Color.RED);
        } else {
            BotUtils.sendMessage(pmChannel, "Max Level Reached!", "You may now prestige and carry over back to level one with `!prestige`."
                    + "\n\nYou will keep all perks, and gain additional unlocks as you level again."
                    + "\n\nPrestiging is **permanent.** Only do so if you are ready.", Color.CYAN);
        }
    }

    //BAD?
    public void prestige(IGuild guild) {
        prestige = prestige.prestige();
        level = 1;
        xp = 0;
        genXPTotalForLevelUp();
        if (!prestige.isMax()) {
            RankManager.verifyRankOfUser(guild, user);
        }
        user.getName().verify(guild);
        BotUtils.sendMessage(guild.getChannelsByName("log").get(0), BotUtils.getMention(user), "PRESTIGE UP!",
                String.format("**%d → %d**", prestige.getNumber() - 1, prestige.getNumber()), Color.BLACK);
        if (prestige.getNumber() == 1) { //messy to put these here?
            BotUtils.sendMessage(guild.getClient().getOrCreatePMChannel(guild.getUserByID(user.getDiscordID())),
                    "Perk Unlocked!", "You have unlocked the ability to change your **name color** on " + guild.getName() + "!"
                    + "\n\n*You can type* `!color` *on the server get started.*", Color.PINK);
        } else if (prestige.isMax()) {
            if (reincarnation.isReincarnated()) {
                BotUtils.sendMessage(guild.getClient().getOrCreatePMChannel(guild.getUserByID(user.getDiscordID())),
                        "Well well.", "**You have reached the maximum prestige once again.** "
                        + "Although this life may have gone by quicker, it was still quite the journey to have gotten here. "
                        + "That said, if you are satisfied and ready to start over with an additional 50% XP boost, "
                        + "you may reincarnate into your next life with `!reincarnate`. Again, the choice is ultimately yours.");
            } else {
                BotUtils.sendMessage(guild.getClient().getOrCreatePMChannel(guild.getUserByID(user.getDiscordID())),
                        "At last.", "**You have reached the maximum prestige.** "
                        + "Your everlasting hard work has earned you the final badge, the **trident**.", Color.BLACK);
                BotUtils.sendMessage(guild.getClient().getOrCreatePMChannel(guild.getUserByID(user.getDiscordID())),
                        "Final Words", "At max prestige, you may level *infinitely* for fun, but you won't unlock anything new."
                        + "\n\nHowever, your path doesn't have to end here. If you are satisfied with the life you have lived, you may **reincarnate.** "
                        + "Your next life will pass by faster with a 50% XP boost, but your level, prestige, badges, and unlocks "
                        + "will be reset completely, and you will have to earn everything anew."
                        + "\n\nIf you are willing to start again, you may do so with `!reincarnate`. The choice is ultimately yours.", Color.BLACK);
            }
        }
    }

    public void reincarnate(IGuild guild) {
        genDefaultStats();
        reincarnation = reincarnation.reincarnate();
        BotUtils.setUserRoles(guild, guild.getUserByID(user.getDiscordID()), ColorManager.getUserRolesNoColors(
                guild.getUserByID(user.getDiscordID()), guild)); //remove color role
        RankManager.verifyRoleOnGuild(guild, user);
        user.getName().setEmoji(0, guild); //remove emoji
        BotUtils.sendMessage(guild.getChannelsByName("log").get(0), BotUtils.getMention(user), "REINCARNATION",
                "**" + reincarnation.getEnglish() + "**", Color.PINK);
    }

    //Moved here until theres a solution/ unlock manager?
    //All of this is hardcoded, clean it up eventually
    private void notifyPossibleUnlocks(IChannel pmChannel, IGuild guild) {
        int totalLevel = getTotalLevelThisLife();
        if (totalLevel % 20 == 0 && !prestige.isMax()) {
            String message = "", title = "";
            Color colorToUse = Color.ORANGE;
            if (totalLevel == 20) {
                title = "Perks Unlocked!";
                message = "You have unlocked the ability to set **tags** and a **description** for yourself on " + guild.getName() + "!"
                        + "\n\n*You can type* `!tag` *and* `!desc` *on the server to get started.*";
            } else if (totalLevel == 40) {
                title = "Perk Unlocked!";
                message = "You have unlocked the ability to set an **emoji** in your name on " + guild.getName() + "!"
                        + "\n\n*You can type* `!emoji` *on the server to get started.*";
            } else if (totalLevel == 60) {
                title = "Perk Unlocked!";
                message = "You have unlocked the ability to change your **nickname** on " + guild.getName() + "!"
                        + "\n\n*You can type* `!nick` *on the server to get started.*";
            } else if (totalLevel > MAX_LEVEL) { //already prestiged, unlock color every 20 levels
                Unlockable color = ColorManager.getUnlockedColor(totalLevel);
                if (color != null) {
                    title = "Color Unlocked!";
                    message = "You have unlocked the name color **" + color.toString() + "** on " + guild.getName() + "!"
                            + "\n\n*You can type* `!color list` *on the server to view your unlocked colors.*";
                    colorToUse = guild.getRolesByName(color.toString()).get(0).getColor();
                }
            }
            BotUtils.sendMessage(pmChannel, title, message, colorToUse);
        }
    }

    private void genDefaultStats() {
        level = 1;
        xp = 0;
        genXPTotalForLevelUp();
        prestige = new Prestige(0);
        rank = RankManager.getRankForLevel(level);
    }

    private void genXPTotalForLevelUp() {
        xpTotalForLevelUp = level * XP_SCALE + XP_FLAT;
    }

}
