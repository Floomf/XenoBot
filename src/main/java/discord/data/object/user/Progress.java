package discord.data.object.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import discord.command.perk.ColorCommand;
import discord.command.perk.DescCommand;
import discord.command.perk.EmojiCommand;
import discord.command.perk.NickCommand;
import discord.data.ColorManager;
import discord.data.object.TempShopItem;
import discord.data.object.Unlockable;
import discord.util.BotUtils;
import discord.util.DiscordColor;
import discord.util.MessageUtils;
import discord4j.core.object.entity.Role;

import java.awt.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Progress {

    public final static int MAX_LEVEL = 80;

    public static double GLOBAL_XP_MULTIPLIER = 1.0;

    private final static int XP_SCALE = 10;
    private final static int XP_FLAT = 50;

    @JsonIgnore
    public DUser user;

    private int level;
    private double xp;
    private int xpTotalForLevelUp;
    @JsonIgnore
    private Rank rank;
    private Prestige prestige;
    private Reincarnation reincarnation;
    @JsonIgnore
    private double xpMultiplier;

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
        this.rank = prestige.isMax() ? Rank.getMaxRank() : Rank.getRankForLevel(level);
        this.xpMultiplier = 1.0;
    }

    protected Progress() {
        setDefaultStats();
        this.reincarnation = new Reincarnation(0);
        this.xpMultiplier = 1.0;
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
        return GLOBAL_XP_MULTIPLIER * xpMultiplier * (0.5 * reincarnation.getNumber() + 1.0);
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
    public int getTotalXPThisLife() {
        return (int) (getTotalXPToPrestige(prestige.getNumber()) + getTotalXPToLevel(level) + xp);
    }

    @JsonIgnore
    public int getTotalXP() {
        return getTotalXPThisLife() + reincarnation.getNumber() * getTotalXPToPrestige(Prestige.MAX_PRESTIGE);
    }

    @JsonIgnore
    public double getXPRate(int users) {
        return 0.5 * getXPMultiplier() * (users + 13);
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
            totalXP += i * XP_SCALE + XP_FLAT;
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
    public void setUser(DUser user) {
        this.user = user;
    }

    public void addXP(double xp) {
        if (isNotMaxLevel()) {
            this.xp += xp;
            checkXP();
        }
    }

    public void addPeriodicXP(int userAmount) {
        addXP(getXPRate(userAmount));
        System.out.println("+" + getXPRate(userAmount) + "xp to " + user.getName().getNick());
    }

    private void checkXP() {
        if (xp >= xpTotalForLevelUp || xp < 0) {
            if (xp >= xpTotalForLevelUp) {
                levelUp();
                checkUnlocks();
            } else if (xp < 0) {
                //TODO support prestiging down?
                if (level > 1) { //prevent negative levels
                    levelDown();
                } else {
                    xp = 0;
                }
            }
            if (!prestige.isMax()) {//keep last rank at max prestige
                changeRankIfNeeded();
            }
            if (isMaxLevel()) {
                maxOut();
            } else {
                checkXP();
            }
        }
    }

    //Only handles leveling up, logic and xp handling handled elsewhere
    private void levelUp() {
        xp -= xpTotalForLevelUp; //carry over xp to next level by subtracting from level xp
        level++;
        genXPTotalForLevelUp();

        MessageUtils.sendMessage(BotUtils.getGuildTextChannel("log", user.asGuildMember().getGuild().block()), "Level up!",
                String.format("%s\n**%d → %d**", user.asGuildMember().getMention(), level - 1, level), user.asGuildMember().getColor().block());
    }

    //same as leveling up method
    private void levelDown() {
        level--;
        genXPTotalForLevelUp();
        xp += xpTotalForLevelUp; //add negative xp to new level xp
        MessageUtils.sendMessage(BotUtils.getGuildTextChannel("log", user.asGuildMember().getGuild().block()), "Level down!",
                String.format("%s\n**%d → %d**", user.asGuildMember().getMention(), level + 1, level), user.asGuildMember().getColor().block());
    }

    private void checkUnlocks() {
        if (user.getPrefs().get(Pref.NOTIFY_UNLOCK)) {
            notifyPossibleUnlocks();
        }
    }

    private void maxOut() {
        xp = 0;
        xpTotalForLevelUp = 0;

        if (user.getPrefs().get(Pref.AUTO_PRESTIGE)) {
            prestige();
            return;
        }

        if (prestige.getNumber() == 0 && !reincarnation.isReincarnated()) { //max level for the first time
            MessageUtils.sendMessage(user.asGuildMember().getPrivateChannel().block(), "Well done!", "You have reached the maximum level for the first time. "
                    + "If you choose to, you may now prestige and carry back over to level one with `!prestige`."
                    + "\n\nYou will keep all perks, and earn your first badge.", DiscordColor.GREEN.getColor());
        } else if (getTotalLevelThisLife() == MAX_LEVEL * Prestige.MAX_PRESTIGE) {
            MessageUtils.sendMessage(user.asGuildMember().getPrivateChannel().block(), "Incredible!", "You have reached the maximum level for the last time "
                    + "and may now move onto the final prestige with `!prestige`.", DiscordColor.RED.getColor());
        } else {
            MessageUtils.sendMessage(user.asGuildMember().getPrivateChannel().block(), "Max Level Reached!",
                    "You may now prestige and carry over back to level one with `!prestige`."
                            + "\n\nYou will keep all perks, and unlock new name colors as you level again.", DiscordColor.GREEN.getColor());
        }
    }

    //BAD?
    public void prestige() {
        prestige = prestige.prestige();
        level = 1;
        xp = 0;
        genXPTotalForLevelUp();
        if (!prestige.isMax()) {
            changeRankIfNeeded();
        }
        user.getName().verifyOnGuild();
        BotUtils.getGuildTextChannel("log", user.asGuildMember().getGuild().block()).createMessage(spec -> {
            spec.setContent(user.asGuildMember().getMention());
            spec.setEmbed(MessageUtils.getEmbed("PRESTIGE UP!", String.format("**%d → %d**",
                    prestige.getNumber() - 1, prestige.getNumber()), Color.BLACK));
        }).block();

        if (prestige.isMax()) {
            if (reincarnation.isMax()) {
                MessageUtils.sendMessage(user.asGuildMember().getPrivateChannel().block(),
                        "The End", "**You have reached the maximum prestige for the final time.** "
                                + "You've lived through many lives and **thousands** of hours to reach this point, "
                                + "and I truly thank you for your dedication to The Realm."
                                + "\n\nThis is finally the end of the road, but you may still level infinitely."
                                + "\n\nFarewell and **congratulations.**", Color.WHITE);
            } else if (reincarnation.isReincarnated()) {
                MessageUtils.sendMessage(user.asGuildMember().getPrivateChannel().block(),
                        "Well well.", "**You have reached the maximum prestige once again.** "
                                + "Although this life may have gone by quicker, it's always a journey to get here. "
                                + "That said, if you are satisfied and ready to start over with an additional 50% XP boost, "
                                + "you may reincarnate into your next life with `!reincarnate`.", Color.BLACK);
            } else {
                MessageUtils.sendMessage(user.asGuildMember().getPrivateChannel().block(),
                        "At last!", "**You have reached the maximum prestige.** "
                                + "Your incredible work has earned you your final badge."
                                + "\n\nAt max prestige, you now level *infinitely* for fun, but your path doesn't have to end here. "
                                + "If you are willing to start over, you may **reincarnate.** "
                                + "You'll begin your next life with a 50% XP boost, but your level and prestige will reset completely, "
                                + "and you'll have to unlock everything again."
                                + "\n\nIf you are ever ready to be reborn, you may do so with `!reincarnate`. The choice is ultimately yours.", Color.BLACK);
            }
        }
    }

    public void reincarnate() {
        reincarnation = reincarnation.reincarnate();
        BotUtils.getGuildTextChannel("log", user.asGuildMember().getGuild().block()).createMessage(spec -> {
            spec.setContent(user.asGuildMember().getMention());
            spec.setEmbed(MessageUtils.getEmbed("REINCARNATION", "**" + reincarnation.getRomaji() + "**", DiscordColor.PINK.getColor()));
        }).block();

        int carryXP = getTotalXPThisLife() - getTotalXPToPrestige(Prestige.MAX_PRESTIGE); //need to store
        setDefaultStats();
        verifyRankOnGuild();
        carryXPToNextLife(carryXP);
        user.getName().verifyOnGuild();

        user.asGuildMember().edit(spec -> spec.setRoles(ColorManager.getMemberRolesNoColor(user.asGuildMember()))).block();
    }

    public void carryXPToNextLife(int xp) {
        int prestigeXP = getTotalXPToLevel(MAX_LEVEL);
        int timesToPrestige = Math.min(xp / prestigeXP, Prestige.MAX_PRESTIGE); //thank you intellij

        //handles sending prestige messages instead of running prestige() multiple times
        for (int i = 1; i <= timesToPrestige; i++) {
            final int p = i;
            BotUtils.getGuildTextChannel("log", user.asGuildMember().getGuild().block()).createMessage(spec -> {
                spec.setContent(user.asGuildMember().getMention());
                spec.setEmbed(MessageUtils.getEmbed("PRESTIGE UP!", String.format("**%d → %d**", p - 1, p), Color.BLACK));
            }).block();
        }

        prestige = new Prestige(timesToPrestige);

        if (prestige.isMax()) { //have to set max rank back
            rank = Rank.getMaxRank();
            verifyRankOnGuild();
        }

        xp -= timesToPrestige * prestigeXP;
        this.xp = xp;
        checkXP(); //check for leveling and ranking up with the remaining xp
    }

    //Moved here until theres a solution/ unlock manager?
    //All of this is hardcoded, clean it up eventually
    private void notifyPossibleUnlocks() {
        int totalLevel = getTotalLevelThisLife();
        if (totalLevel % 20 == 0 && !prestige.isMax()) {
            if (!prestige.isPrestiged()) {
                String perkDesc = "", perkCommand = "";
                if (totalLevel == DescCommand.LEVEL_REQUIRED) {
                    perkCommand = "!desc";
                    perkDesc = "set your **profile description**";
                } else if (totalLevel == NickCommand.LEVEL_REQUIRED) {
                    perkCommand = "!nick";
                    perkDesc = "change your **nickname**";
                } else if (totalLevel == EmojiCommand.LEVEL_REQUIRED) {
                    perkCommand = "!emoji";
                    perkDesc = "set **emojis** in your name";
                } else if (totalLevel == ColorCommand.LEVEL_REQUIRED) {
                    perkCommand = "!color";
                    perkDesc = "change your **name color**";
                }
                MessageUtils.sendMessage(user.asGuildMember().getPrivateChannel().block(), "Perk Unlocked!",
                        "You have unlocked the ability to " + perkDesc + " on " + user.asGuildMember().getGuild().block().getName()
                                + "!\n\n*You can type `" + perkCommand + "` on the server to get started.*", DiscordColor.ORANGE.getColor());

            } else { //already prestiged, unlock new color every 20 levels
                Unlockable color = ColorManager.getUnlockedColor(totalLevel);
                if (color != null) {
                    MessageUtils.sendMessage(user.asGuildMember().getPrivateChannel().block(), "Color Unlocked!",
                            "You have unlocked the name color **" + color.toString() + "** on "
                                    + user.asGuildMember().getGuild().block().getName()
                                    + "!\n\n*You can type `!color list` on the server to view your unlocked colors.*",
                            BotUtils.getGuildRole(color.toString(), user.asGuildMember().getGuild().block()).getColor());
                }
            }
        }
    }

    //Rank logic can't be managed in rank objects because only one exists for each rank, so user objects cant be set
    //TODO Design a different system sometime
    protected void changeRankIfNeeded() {
        Rank rankNeeded = Rank.getRankForLevel(level);
        if (!rankNeeded.equals(rank)) {
            if (!rankNeeded.equals(Rank.RANKS[0])) {
                if (user.getPrefs().get(Pref.MENTION_RANKUP)) { //TODO clean up this up
                    BotUtils.getGuildTextChannel("log", user.asGuildMember().getGuild().block()).createMessage(spec -> {
                        spec.setContent(user.asGuildMember().getMention());
                        spec.setEmbed(MessageUtils.getEmbed("Rank up!", "**" + rank.getName() + " → " + rankNeeded.getName() + "**",
                                user.asGuildMember().getColor().block()));
                    }).block();
                } else {
                    MessageUtils.sendMessage(BotUtils.getGuildTextChannel("log", user.asGuildMember().getGuild().block()), "Rank up!",
                            user.asGuildMember().getMention() + "\n**" + rank.getName() + " → " + rankNeeded.getName() + "**",
                            user.asGuildMember().getColor().block());
                }
            }
            rank = rankNeeded;
            verifyRankOnGuild();
        }
    }

    public void verifyRankOnGuild() {
        System.out.println("Verifying rank (" + rank.getName() + ") for " + user.getName());
        List<Role> roles = user.asGuildMember().getGuild().block().getRoles()
                .filter(role -> role.getName().equals(rank.getRoleName()))
                .collectList().block();
        if (roles.isEmpty()) {
            System.out.println("Couldn't find role " + rank.getRoleName() + " for rank " + rank.getName() + " on guild! Please create one.");
            return;
        }
        Role rankRole = roles.get(0);
        List<Role> memberRoles = user.asGuildMember().getRoles().collectList().block();

        if (!memberRoles.contains(rankRole)) {
            for (Rank rank : Rank.RANKS) { //remove all existing rank roles
                memberRoles.removeIf(role -> role.getName().equals(rank.getRoleName()));
            }
            memberRoles.add(rankRole);
            user.asGuildMember().edit(spec -> spec.setRoles(memberRoles.stream().map(Role::getId).collect(Collectors.toSet()))).block();
            System.out.println("Updated rank role to " + rankRole.getName());
        }
    }

    protected void onPurchaseMultiplier(TempShopItem item) {
        double tempMultiplier;
        if (item.getName().toLowerCase().startsWith("triple")) { //little hacky
            tempMultiplier = 3.0;
        } else {
            tempMultiplier = 2.0;
        }
        xpMultiplier = tempMultiplier;
        user.asGuildMember().getPrivateChannel().block().createEmbed(MessageUtils.getEmbed("Info",
                "You will now receive **" + tempMultiplier + "x** XP for the next **" + item.getHours() + "h**.", DiscordColor.CYAN.getColor())).block();

        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        xpMultiplier = 1.0;
                        user.asGuildMember().getPrivateChannel().block().createEmbed(MessageUtils.getEmbed("Info",
                                "Your purchased XP boost has run out.", DiscordColor.CYAN.getColor())).block();
                        user.getPurchases().remove(item);
                    }
                }, TimeUnit.HOURS.toMillis(item.getHours())
        );
    }

    private void setDefaultStats() {
        level = 1;
        xp = 0;
        genXPTotalForLevelUp();
        prestige = new Prestige(0);
        rank = Rank.getRankForLevel(level);
    }

    private void genXPTotalForLevelUp() {
        xpTotalForLevelUp = level * XP_SCALE + XP_FLAT;
    }
}
