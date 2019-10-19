 package discord.util;

import discord.data.object.Prestige;
import discord.data.object.Progress;
import discord.data.object.User;
import java.text.DecimalFormat;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

public class ProfileBuilder {
    
    private final EmbedBuilder builder;
    private final User user;
    private final Progress progress;
    
    public ProfileBuilder(IGuild guild, User user) {
        this.user = user;
        this.builder = BotUtils.getBaseBuilder(guild.getClient());
        this.progress = user.getProgress();
        setupBase(guild);
    }
    
    private void setupBase(IGuild guild) {
        IUser dUser = guild.getClient().fetchUser(user.getDiscordID()); //temporary?
        builder.withAuthorName(user.getName().getNick());
        builder.withColor(dUser.getColorForGuild(guild));
        builder.withThumbnail(dUser.getAvatarURL());
    }
    
    public ProfileBuilder addDesc() {
        builder.withDesc("*`" + user.getDesc() + "`*");
        return this;
    }
    
    public ProfileBuilder addRank() {
        builder.appendField("Rank :trident:", "`" + progress.getRank().getName()
                + (progress.getRank().isMax() ? "` (Max)" : "`"), true);
        return this;
    }
    
    public ProfileBuilder addLevel() {
        builder.appendField("Level :gem:", "`" + progress.getLevel() 
                + (progress.isMaxLevel() ? "` (Max)" : "`"), true);
        return this;
    }
    
    public ProfileBuilder addXPProgress() {
        DecimalFormat formatter = new DecimalFormat("#.###");
        builder.appendField("XP :diamond_shape_with_a_dot_inside:", "`" 
                + formatter.format(progress.getXP()) + " / " + progress.getXpTotalForLevelUp() + "`", true); 
        return this;
    }
    
    public ProfileBuilder addPrestige() {
        builder.appendField("Prestige :trophy:", "`" + progress.getPrestige().getNumber() + "`"
                + progress.getPrestige().getBadge()
                + (progress.getPrestige().isMax() ? " (Max)" : ""), true);
        return this;
    }
    
    public ProfileBuilder addReincarnation() {
        builder.appendField("Reincarnation :white_flower:", "`" + progress.getReincarnation().getKanji()
                + " (" + progress.getReincarnation().getRomaji() + ")`", true);
        return this;
    }
    
    public ProfileBuilder addBarProgressToNextLevel() {
        int percentage = (int) Math.floor((double) progress.getXP() 
                / progress.getXpTotalForLevelUp() * 100); //percentage calc
        builder.appendField(percentage + "% to Level " + (progress.getLevel() + 1) 
                + " :chart_with_upwards_trend:", drawBarProgress(percentage), false);
        return this;
    }
    
    public ProfileBuilder addBarProgressToMaxLevel() {
        int currentTotalXP = Progress.getTotalXPToLevel(progress.getLevel()) + (int) progress.getXP();
        int maxXP = Progress.getTotalXPToLevel(Progress.MAX_LEVEL);
        int percentage = (int) Math.floor((double) currentTotalXP / maxXP * 100);
        builder.appendField(percentage + "% to Max Level :checkered_flag:", 
                drawBarProgress(percentage), false);
        return this;
    }
    
    public ProfileBuilder addTotalLevel() {
        builder.appendField("Total Level :arrows_counterclockwise:", 
                "`" + String.format("%,d", progress.getTotalLevel()) + "`", true);
        return this;
    }
    
    public ProfileBuilder addTotalXP() {
        builder.appendField("Total XP :clock4:", "`" + String.format("%,d", progress.getTotalXP()) + "`", true);
        return this;
    }
    
    public ProfileBuilder addBadgeCase() {
        builder.appendField("Badge Case :beginner: ", getUserBadges(), true);
        return this;
    }
    
    public ProfileBuilder addXPBoost() {
        builder.appendField("XP Boost :rocket:", "`" + (int)((progress.getXPMultiplier() - 1) * 100) 
                + "% (" + progress.getXPMultiplier() + "x)`", true); //multiplier to percent
        return this;
    }
    
    public ProfileBuilder addXPRate(int users) {
        builder.appendField("Current Rate :hourglass_flowing_sand:", "`" + progress.getXPRate(users) + " XP/min`", true);
        return this;
    }
    
    public EmbedObject build() {
        return builder.build();
    }   
    
    private String getUserBadges() {
        String badges = "";
        for (int i = 1; i <= progress.getPrestige().getNumber(); i++) {
            badges += Prestige.BADGES[i];
        }
        return badges;
    } 
     
    private String drawBarProgress(int percentage) {
        StringBuilder sb = new StringBuilder();
        //generate an int 1-10 depicting progress based on percentage
        int prog = percentage / 10;
        for (int i = 1; i <= 10; i++) {
            if (i <= prog)
                sb.append("\u2B1C"); //white square emoji
            else
                sb.append("\u2B1B"); //black square emoji
        }
        return sb.toString();
    }
    
}
