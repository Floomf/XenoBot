package discord.data.object.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import discord.data.object.ShopItem;
import discord.data.object.Unlockable;
import discord4j.core.object.entity.Member;

import java.util.ArrayList;
import java.util.HashMap;

public class DUser {

    private final long discordID;
    private final Name name;
    private String desc;
    private final Progress prog;
    private final HashMap<Pref, Boolean> prefs;

    private int[] birthday;

    private int balance;
    private final ArrayList<ShopItem> purchases;

    //we want this final but can't deserialize it?
    @JsonIgnore
    private Member guildMember;

    @JsonCreator
    public DUser(@JsonProperty("discordID") long discordID,
                 @JsonProperty("name") Name name,
                 @JsonProperty("desc") String desc,
                 @JsonProperty("prog") Progress progress,
                 @JsonProperty("prefs") HashMap<Pref, Boolean> prefs,
                 @JsonProperty("balance") int balance,
                 @JsonProperty("purchases") ArrayList<ShopItem> purchases,
                 @JsonProperty("birthday") int[] birthday) {
        this.discordID = discordID;
        this.name = name;
        this.prog = progress;
        this.desc = desc;
        this.prefs = prefs;
        this.balance = balance;
        this.purchases = purchases;
        this.birthday = birthday;
        this.name.setUser(this);
        this.prog.setUser(this);
    }

    public DUser(Member guildMember, String nick) {
        this.guildMember = guildMember;
        this.discordID = guildMember.getId().asLong();
        this.name = new Name(nick);
        this.desc = "";
        this.prog = new Progress();
        this.prefs = new HashMap<>();
        this.prefs.put(Pref.MENTION_RANKUP, true);
        this.prefs.put(Pref.NOTIFY_UNLOCK, true);
        this.prefs.put(Pref.AUTO_PRESTIGE, false);
        this.balance = 100;
        this.purchases = new ArrayList<>();
        this.birthday = null;
        this.name.setUser(this);
        this.prog.setUser(this);
        verifyOnGuild();
    }

    public long getDiscordID() {
        return discordID;
    }

    public Member asGuildMember() {
        return guildMember;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Progress getProg() {
        return prog;
    }

    public Name getName() {
        return name;
    }

    public HashMap<Pref, Boolean> getPrefs() {
        return prefs;
    }

    public int getBalance() {
        return balance;
    }

    //for jackson only, don't know how else to save it
    public ArrayList<ShopItem> getPurchases() {
        return purchases;
    }

    public int[] getBirthday() {
        return birthday;
    }

    @JsonIgnore
    public void setGuildMember(Member guildMember) {
        this.guildMember = guildMember;
    }

    public void setBirthday(int day, int month, int year) {
        this.birthday = new int[] {day, month, year};
    }

    public void addBalance(int balToAdd) {
        balance += balToAdd;
    }

    public void purchase(ShopItem item) {
        balance -= item.getPrice();
        purchases.add(item);
        if (item.getCategory() == ShopItem.Category.XP_MULTIPLIER) {
            prog.onPurchaseMultiplier(item);
        }
    }

    public boolean canPurchase(ShopItem item) {
        return balance >= item.getPrice();
    }

    public boolean hasPurchased(ShopItem item) {
        return hasPurchased(item.getName());
    }

    public boolean hasPurchased(String name) {
        return purchases.stream().anyMatch(item -> item.getName().equalsIgnoreCase(name));
    }

    public boolean hasUnlocked(Unlockable unlockable) {
        return prog.getTotalLevelThisLife() >= unlockable.getTotalLevelRequired();
    }

    //will fuck up if member isnt set
    public void verifyOnGuild() {
        prog.verifyRankOnGuild();
        name.verifyOnGuild();
    }
}
