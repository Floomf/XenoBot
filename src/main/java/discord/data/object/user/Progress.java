package discord.data.object.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import discord.command.perk.ColorCommand;
import discord.command.perk.EmojiCommand;
import discord.command.perk.NickCommand;
import discord.data.object.ShopItem;
import discord.manager.ColorManager;
import discord.data.object.Unlockable;
import discord.util.BotUtils;
import discord.util.DiscordColor;
import discord.util.MessageUtils;
import discord4j.core.object.entity.Role;
import discord4j.core.retriever.EntityRetrievalStrategy;
import discord4j.rest.util.Color;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Progress {

    final static Unlockable[] COMMAND_UNLOCKS = {
            Unlockable.command("/name", 0, NickCommand.LEVEL_REQUIRED),
            Unlockable.command("/emoji", 0, EmojiCommand.LEVEL_REQUIRED),
            Unlockable.command("/color", 0, ColorCommand.LEVEL_REQUIRED)
    };

    public final static Unlockable[] BADGE_UNLOCKS = {
            Unlockable.badge("★", 1, 1),
            Unlockable.badge("✷", 2, 1),
            Unlockable.badge("⁂", 3, 1),
            Unlockable.badge("❖", 4, 1),
            Unlockable.badge("✪", 5, 1),
            Unlockable.badge("❃", 6, 1),
            Unlockable.badge("❈", 7, 1),
            Unlockable.badge("✠", 8, 1),
            Unlockable.badge("♆", 9, 1),
            Unlockable.badge("֎", 10, 1),

            Unlockable.badge("🟆", 10, 100),
            Unlockable.badge("⨳", 10, 200),
            Unlockable.badge("✺", 10, 300),
            Unlockable.badge("⟁", 10, 400),
            Unlockable.badge("▣", 10, 500),
            Unlockable.badge("🞛", 10, 600),
            Unlockable.badge("⧉", 10, 700),
            Unlockable.badge("❀", 10, 800),
            Unlockable.badge("✾", 10, 900),
            Unlockable.badge("☬", 10, 1000),
            Unlockable.badge("♙", 10, 1100),
            Unlockable.badge("♘", 10, 1200),
            Unlockable.badge("♔", 10, 1300),
            Unlockable.badge("ꕥ", 10, 1400),
            Unlockable.badge("⸎", 10, 1500)
    };

    public final static int MAX_LEVEL = 80;

    public static double GLOBAL_XP_MULTIPLIER = 1.0;

    private final static int XP_SCALE = 10;
    private final static int XP_FLAT = 50;

    @JsonIgnore
    public DUser user;

    private int level;
    private double xp;
    @JsonIgnore
    private int xpTotalForLevelUp;
    @JsonIgnore
    private Rank rank;
    private Prestige prestige;
    private Reincarnation reincarnation;
    @JsonIgnore
    private double xpMultiplier;
    //private double purchasedBoosts;

    @JsonCreator
    protected Progress(@JsonProperty("level") int level,
                       @JsonProperty("xp") int xp,
                       @JsonProperty("prestige") Prestige prestige,
                       @JsonProperty("reincarnation") Reincarnation reincarnation) {
        this.level = level;
        this.xp = xp;
        this.prestige = prestige;
        this.reincarnation = reincarnation;
        this.rank = prestige.isMax() ? Rank.getMaxRank() : Rank.getRankForLevel(level);
        this.xpMultiplier = 1.0;
        //this.purchasedBoosts = 0.0;
        genXPTotalForLevelUp();
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
    public String[] getBadges() { //TODO change
        ArrayList<Unlockable> badges = new ArrayList<>(Arrays.asList(BADGE_UNLOCKS));
        badges.removeIf(badge -> getTotalLevelThisLife() < badge.getTotalLevelRequired());
        return badges.stream().map(Unlockable::toString).toArray(String[]::new);
    }

    public static int getBadgeIndex(String badge) {
        for (int i = 0; i < BADGE_UNLOCKS.length; i++) {
            if (badge.equals(BADGE_UNLOCKS[i].toString())) {
                return i;
            }
        }
        return -1;
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
    }

    private void checkXP() {
        checkXP(true);
    }

    private void checkXP(boolean logToChannel) {
        if (xp >= xpTotalForLevelUp || xp < 0) {
            if (xp >= xpTotalForLevelUp) {
                levelUp(logToChannel);
                checkAllUnlocks();
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
                checkXP(logToChannel);
            }
        }
    }

    //Only handles leveling up, logic and xp handling handled elsewhere
    private void levelUp(boolean logToChannel) {
        xp -= xpTotalForLevelUp; //carry over xp to next level by subtracting from level xp
        level++;
        genXPTotalForLevelUp();

        if (logToChannel && (!reincarnation.isReincarnated() || prestige.isMax())) {
            MessageUtils.sendMessage(BotUtils.getGuildTextChannel("log", user.asGuildMember().getGuild().block()), "Level up!",
                    String.format("%s\n**%d → %d**", user.asGuildMember().getMention(), level - 1, level), user.asGuildMember().getColor().block());
        }
    }

    //same as leveling up method
    private void levelDown() {
        level--;
        genXPTotalForLevelUp();
        xp += xpTotalForLevelUp; //add negative xp to new level xp
        MessageUtils.sendMessage(BotUtils.getGuildTextChannel("log", user.asGuildMember().getGuild().block()), "Level down!",
                String.format("%s\n**%d → %d**", user.asGuildMember().getMention(), level + 1, level), user.asGuildMember().getColor().block());
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
                    + "If you choose to, you may now prestige and carry back over to level one with `/prestige`."
                    + "\n\nYou will keep all perks, and earn your first badge.", DiscordColor.GREEN);
        } else if (getTotalLevelThisLife() == MAX_LEVEL * Prestige.MAX_PRESTIGE) {
            MessageUtils.sendMessage(user.asGuildMember().getPrivateChannel().block(), "Incredible!", "You have reached the maximum level for the last time "
                    + "and may now move onto the final prestige with `/prestige`.", DiscordColor.RED);
        } else {
            MessageUtils.sendMessage(user.asGuildMember().getPrivateChannel().block(), "Max Level Reached!",
                    "You may now prestige and carry over back to level one with `/prestige`."
                            + "\n\nYou will keep all perks, and unlock new name colors as you level again.", DiscordColor.GREEN);
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
        checkUnlocks(BADGE_UNLOCKS);
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
                                + "You've lived through many lives and countless hours to reach this point, "
                                + "and I truly thank you for your dedication to The Realm."
                                + "\n\nThis is your last life, but you will still level infinitely."
                                + "\n\n**Congratulations.**", Color.WHITE);
            } else if (reincarnation.isReincarnated()) {
                MessageUtils.sendMessage(user.asGuildMember().getPrivateChannel().block(),
                        "Well well.", "**You have reached the maximum prestige once again.** "
                                + "Although this life may have gone by quicker, it's always a journey to get here. "
                                + "That said, if you are satisfied and ready to start over with an additional 50% XP boost, "
                                + "you may reincarnate into your next life with `/reincarnate`.", Color.BLACK);
            } else {
                MessageUtils.sendMessage(user.asGuildMember().getPrivateChannel().block(),
                        "At last!", "**You have reached the maximum prestige.** "
                                + "Your incredible work has earned you your final badge."
                                + "\n\nAt max prestige, you now level *infinitely* for fun, but your path doesn't have to end here. "
                                + "If you are willing to start over, you may **reincarnate.** "
                                + "You'll begin your next life with a 50% XP boost, but your level and prestige will reset completely, "
                                + "and you'll have to unlock everything again."
                                + "\n\nIf you are ever ready to be reborn, you may do so with `/reincarnate`. The choice is ultimately yours.", Color.BLACK);
            }
        }
    }

    public void reincarnate() {
        reincarnation = reincarnation.reincarnate();

        if (reincarnation.getNumber() == 1) {
            user.getPrefs().put(Pref.MENTION_RANKUP, false);
            user.getPrefs().put(Pref.AUTO_PRESTIGE, true);
        } else if (reincarnation.getNumber() == 2) {
            user.getPrefs().put(Pref.NOTIFY_UNLOCK, false);
        }

        BotUtils.getGuildTextChannel("log", user.asGuildMember().getGuild().block()).createMessage(spec -> {
            spec.setContent(user.asGuildMember().getMention());
            spec.setEmbed(MessageUtils.getEmbed("REINCARNATION", "**" + reincarnation.getRomaji() + "**", DiscordColor.PINK));
        }).block();

        int carryXP = getTotalXPThisLife() - getTotalXPToPrestige(Prestige.MAX_PRESTIGE); //need to store
        setDefaultStats();
        verifyRankOnGuild();
        carryXPToNextLife(carryXP);
        user.getName().setBadge("");

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

        if (prestige.getNumber() > 0) {
            user.getName().setBadge(BADGE_UNLOCKS[prestige.getNumber() - 1].toString());
        }

        if (prestige.isMax()) { //have to set max rank back
            rank = Rank.getMaxRank();
            verifyRankOnGuild();
        }

        xp -= timesToPrestige * prestigeXP;
        this.xp = xp;
        checkXP(false); //check for leveling and ranking up with the remaining xp
    }

    //A better solution, but im sure can still be made better
    private void checkAllUnlocks() {
        int totalLevel = getTotalLevelThisLife();
        if (totalLevel <= MAX_LEVEL && totalLevel % 10 == 0) { //unlocking commands, only first prestige
            checkUnlocks(COMMAND_UNLOCKS);
        } else if (totalLevel % 20 == 0 && !prestige.isMax()) { //unlocking colors, every 20 levels
            checkUnlocks(ColorManager.getUnlockableColors());
        } else if (level == 1 || level % 100 == 0) { //unlocking badges, every prestige and then every 100 levels after max prestige
            checkUnlocks(BADGE_UNLOCKS);
        }
    }

    private void checkUnlocks(Unlockable[] unlockables) {
        for (Unlockable command : unlockables) {
            if (command.getTotalLevelRequired() == getTotalLevelThisLife()) {
                command.onUnlock(user);
                return;
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
        Role rankRole = user.asGuildMember().getGuild().block().getRoles()
                .filter(role -> role.getName().equals(rank.getRoleName()))
                .blockFirst();

        List<Role> memberRoles = user.asGuildMember().getRoles(EntityRetrievalStrategy.REST).collectList().block();

        if (!memberRoles.contains(rankRole)) {
            for (Rank rank : Rank.RANKS) { //remove all existing rank roles
                memberRoles.removeIf(role -> role.getName().equals(rank.getRoleName()));
            }
            memberRoles.add(rankRole);
            user.asGuildMember().edit(spec -> spec.setRoles(memberRoles.stream().map(Role::getId).collect(Collectors.toSet()))).block();
            System.out.println("Updated rank role to " + rankRole.getName());
        }
    }

    protected void onPurchaseMultiplier(ShopItem item) {
        double tempMultiplier;
        if (item.getName().toLowerCase().startsWith("triple")) { //little hacky
            tempMultiplier = 3.0;
        } else {
            tempMultiplier = 2.0;
        }
        xpMultiplier = tempMultiplier;
        MessageUtils.sendMessage(user.asGuildMember().getPrivateChannel().block(), "Info",
                "You will now receive **" + tempMultiplier + "x** XP for the next **" + item.getDuration() + "h**.", DiscordColor.CYAN);

        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        xpMultiplier = 1.0;
                        MessageUtils.sendMessage(user.asGuildMember().getPrivateChannel().block(), "Info",
                                "Your purchased XP boost has run out.", DiscordColor.CYAN);
                        user.getPurchases().remove(item);
                    }
                }, TimeUnit.HOURS.toMillis(item.getDuration())
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
