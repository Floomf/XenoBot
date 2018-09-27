package discord.object;

import com.fasterxml.jackson.annotation.JsonProperty;
import sx.blah.discord.handle.obj.IGuild;

public class User {
    
    @JsonProperty("discordID")
    private final long discordID;
    private String name;
    //private int level;
    //private int xp;
    //private int xpforLevel;
    //private int prestige;
    private Progress progress;
    private int emojiCodePoint;
    //private Rank rank;
    
    public User() {
        this.discordID = 0;   
    }
    
    public User(long discordID, String name) {
        this.discordID = discordID;
        this.name = name;
        //this.rank = rank;
        //level = 1;
        //xp = 0;
        //prestige = 0;
        emojiCodePoint = 0;
        progress = new Progress();
    }
    
    //Accessors
    
    @JsonProperty("discordID")
    public long getID() {
        return discordID;
    }
    
    public Progress getProgress() {
        return progress;
    }
    
    public String getName() {
        return name;
    }
    /*
    public int getLevel() {
        return level;
    }
    
    @JsonIgnore
    public int getTotalLevels() {
        return prestige * 80 + level;
    }
    
    public int getXP() {
        return xp;
    }
    
    public int getXPForLevel() {
        return xpforLevel;
    }
    
    public int getPrestige() {
        return prestige;
    }
    
    public Rank getRank() {
        return rank;
    }
    */
    public int getEmoji() {
        return emojiCodePoint;
    }
  
    //Mutators
    
    public void setProgress(Progress progress) {
        this.progress = progress;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    /*
    public void setRank(Rank rank) {
        this.rank = rank;
    }
    
    public void setXPForLevel(int xpforLevel) {
        this.xpforLevel = xpforLevel;
    }
    
    public void setXP(int xp) {
        this.xp = xp;
    }
    
    public void setLevel(int level) {
        this.level = level;
    }
    */
     public void setEmoji(int codepoint) {
        emojiCodePoint = codepoint;
    }
    /*
    public void addLevels(int amount) {
        level += amount;         
    }
    */
    public void addXP(int amount, IGuild guild) {
        progress.addXP(amount, guild, this);
    }    
    
    public void prestige(IGuild guild) {
        progress.prestige(guild, this);
    }
}
