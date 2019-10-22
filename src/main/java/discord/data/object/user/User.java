package discord.data.object.user;

import discord.data.object.user.Name;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import discord.data.object.Unlockable;
import java.util.HashMap;

public class User {
    
    private final long discordID;
    private final Name name;
    private String desc;
    private final Progress progress;
    private final HashMap<Pref, Boolean> prefs;
    
    @JsonCreator
    public User(@JsonProperty("discordID") long discordID, 
            @JsonProperty("name") Name name,
            @JsonProperty("desc") String desc,
            @JsonProperty("progress") Progress progress,
            @JsonProperty("prefs") HashMap<Pref, Boolean> prefs) {
        this.discordID = discordID;
        this.name = name;
        this.progress = progress;
        this.desc = desc;
        this.prefs = prefs;
        this.name.setUser(this);
        this.progress.setUser(this);
    }
          
    public User(long discordID, String name) {
        this.discordID = discordID;
        this.name = new Name(name);
        this.desc = "";
        this.progress = new Progress();
        this.prefs = new HashMap<>();
        this.prefs.put(Pref.MENTION_RANKUP, true);
        this.prefs.put(Pref.NOTIFY_UNLOCK, true);
        this.prefs.put(Pref.AUTO_PRESTIGE, false);
        this.name.setUser(this);
        this.progress.setUser(this);
    }
      
    public long getDiscordID() {
        return discordID;
    }
    
    public String getDesc() {
        return desc;
    }
    
    public Progress getProgress() {
        return progress;
    }
    
    public Name getName() {
        return name;
    }
    
    public HashMap<Pref, Boolean> getPrefs() {
        return prefs;
    }
    
    public void setDesc(String desc) {
        this.desc = desc;
    }
  
    public boolean hasUnlocked(Unlockable unlockable) {
        return progress.getTotalLevelThisLife() >= unlockable.getTotalLevelRequired();
    }
}
