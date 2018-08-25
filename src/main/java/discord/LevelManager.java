package discord;

import discord.object.User;
import java.awt.Color;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

public class LevelManager {
    
    public static final int MAX_LEVEL = 80;
    public static final int MAX_PRESTIGE = 10; //unused for now
    public static final char[] PRESTIGE_SYMBOLS = {'★','✷','⁂','❖','❃','✠','✪','☭','☬','♆'};
    
    public static void addAndCheckUserXP(IGuild guild, User user, int amount) {
        user.addXP(amount);
        checkXPOfUser(guild, user);
    }
    
    public static void checkXPOfUser(IGuild guild, User user) {
        int xp = user.getXP();
        if (xp >= user.getXPForLevel()) {
            levelUpUser(guild, user);
            checkUnlocksForUser(guild, user);
        } else if (xp < 0) {
            levelDownUser(guild, user);
        }      
        if (xp >= user.getXPForLevel() || xp < 0) { //finally do these
            RankManager.setRankOfUser(guild, user);
            if (user.getLevel() < MAX_LEVEL) checkXPOfUser(guild, user);
        }
    }
    
    //Only handles leveling up, logic and xp handling handled elsewhere
    private static void levelUpUser(IGuild guild, User user) {
        user.addXP(-user.getXPForLevel()); //carry over xp to next level by subtracting from level xp
        user.addLevels(1);
        setUserXPForLevel(user);
        BotUtils.sendMessage(guild.getChannelsByName("log").get(0), "Level up!",
                String.format("%s\n**%d → %d**", BotUtils.getMention(user), 
                        user.getLevel() - 1, user.getLevel()), Color.ORANGE);
    }
    
    //same as leveling up method
    private static void levelDownUser(IGuild guild, User user) {
        user.addLevels(-1);
        setUserXPForLevel(user); 
        user.addXP(user.getXPForLevel()); //add negative xp to new level xp
        BotUtils.sendMessage(guild.getChannelsByName("log").get(0), "Level down!",
                String.format("%s\n**%d → %d**", BotUtils.getMention(user), 
                        user.getLevel(), user.getLevel() - 1), Color.RED);
    }
    
    private static void checkUnlocksForUser(IGuild guild, User user) {
        int level = user.getLevel();
        if (level % 20 == 0) {
            IChannel pmChannel = guild.getClient().getOrCreatePMChannel(guild.getUserByID(user.getID()));
            notifyUnlocksForUser(pmChannel, user);
            if (level == MAX_LEVEL) {
                //send DM to user
                maxOutUser(pmChannel, user);
            }
        }
    }
    
    //All of this is hardcoded, clean it up eventually
    private static void notifyUnlocksForUser(IChannel channel, User user) {
        String message = "";
        if (user.getPrestige() == 0) {
            int level = user.getLevel();
            if (level == 40) {
                message = "You have unlocked the ability to **set an emoji** in your name on the Realm!"
                        + "\n\n*You can type* `!emoji` *on the server to get started.*";
            } else if (level == 60) {
                message = "You have unlocked the ability to **change your nickname** on The Realm!"
                        + "\n\n*You can type* `!name` *on the server to get started.*";
            }
        } else { //already prestiged
            discord.object.Color color = ColorManager.getUnlockedColor(user.getTotalLevels());
            if (color != null) {
                message = "You have unlocked the name color **" + color.getName() + "** on The Realm!"
                        + "\n\n*You can type* `!color list` *on the server to view your unlocked colors.*";
            }
        }
        BotUtils.sendMessage(channel, "Congratulations!", message, Color.ORANGE);
    }
    
    private static void maxOutUser(IChannel channel, User user) {
        user.setXPForLevel(0);
        user.setXP(0);
        BotUtils.sendMessage(channel, "Congratulations!", "You have reached the max level. "
                + "*You will no longer earn **any more xp** until prestiged.*"
                + "\n\nYou can now prestige and carry over back to level one with `!prestige`"
                + "\n\nYou will keep all level perks, and gain additional unlocks as you level again."
                + "\n\nPrestiging is **PERMANENT.** Only do so if you are ready.", Color.CYAN);
    }
    
    public static void prestigeUser(IChannel channel, User user) {
        user.addPrestige();
        user.setLevel(1);
        user.setXP(0);
        setUserXPForLevel(user);
        IGuild guild = channel.getGuild();
        NameManager.formatNameOfUser(guild, user);
        RankManager.setRankOfUser(guild, user);
        BotUtils.sendMessage(channel, "@here", "ALERT", 
                user.getName() + " is now **Prestige " + user.getPrestige() 
                        + "!** Praise unto thee.", Color.CYAN);
        //bad to put it here, organize unlock messages in future
        if (user.getPrestige() == 1) {
            BotUtils.sendMessage(guild.getClient().getOrCreatePMChannel(guild.getUserByID(user.getID())), 
                    "Congratulations!", "You have unlocked the ability to **change your name color** on The Realm!"
                        + "\n\n*You can type* `!color` *on the server get started.*");
        }
    }
    
    public static void setUserXPForLevel(User user) {
        user.setXPForLevel(user.getLevel() * 10 + 50);       
    }
    
    public static EmbedObject buildUserInfo(IGuild guild, User user, IUser dUser) {
        EmbedBuilder builder = BotUtils.getBuilder(user.getName(), 
                user.getRank().getName(), dUser.getColorForGuild(guild));
        builder.withThumbnail(dUser.getAvatarURL());
        builder.appendField("Level :gem:", "`" + user.getLevel() + "`", true);
        int prestige = user.getPrestige();
        if (prestige > 0) {
            builder.appendField("Prestige :trophy:", String.format("`%d%c`", 
                    prestige, PRESTIGE_SYMBOLS[prestige - 1]), true);
            builder.appendField("Total Level :clock4:", "`" + user.getTotalLevels() + "`", true);
        }
        builder.appendField("XP :diamond_shape_with_a_dot_inside:", "`" 
                + user.getXP() + "/" + user.getXPForLevel() + "`", true);
        int percentage = (int) Math.round((double) user.getXP() / user.getXPForLevel() * 100); //percentage calc
        builder.appendField(percentage + "% to Level " + (user.getLevel() + 1), 
                getBarProgress(percentage), false);
        
        return builder.build();
    }
    
    private static String getBarProgress(int percentage) {
        StringBuilder builder = new StringBuilder();
        //generate an int 1-10 depicting progress based on percentage
        int prog = percentage / 10;       
        for (int i = 1; i <= 10; i++) {
            if (i <= prog)
                builder.append(":white_large_square: ");
            else //different emojis handled by discord
                builder.append(":white_square_button: ");
        }
        return builder.toString();
    }
      
}