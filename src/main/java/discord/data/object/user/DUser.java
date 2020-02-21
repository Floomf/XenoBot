package discord.data.object.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import discord.data.object.Unlockable;
import discord4j.core.object.entity.Member;

import java.util.HashMap;

public class DUser {

    private final long discordID;
    private final Name name;
    private final Progress prog;
    private final HashMap<Pref, Boolean> prefs;

    private String desc;

    //we want this final but can't deserialize it?
    @JsonIgnore
    private Member guildMember;

    @JsonCreator
    public DUser(@JsonProperty("discordID") long discordID,
                 @JsonProperty("name") Name name,
                 @JsonProperty("desc") String desc,
                 @JsonProperty("prog") Progress progress,
                 @JsonProperty("prefs") HashMap<Pref, Boolean> prefs) {
        this.discordID = discordID;
        this.name = name;
        this.prog = progress;
        this.desc = desc;
        this.prefs = prefs;
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

    @JsonIgnore
    public void setGuildMember(Member guildMember) {
        this.guildMember = guildMember;
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
