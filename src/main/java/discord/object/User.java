package discord.object;

import com.fasterxml.jackson.annotation.JsonProperty;
import sx.blah.discord.handle.obj.IGuild;

public class User {
    
    @JsonProperty("discordID")
    private final long discordID;
    private String name;
    private Progress progress;
    private int emojiCodePoint;
    
    public User() {
        this.discordID = 0;   
    }
    
    public User(long discordID, String name) {
        this.discordID = discordID;
        this.name = name;
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
    
    public void setEmoji(int codepoint) {
        emojiCodePoint = codepoint;
    }
    
    //this can't be in progress because it needs the user object, find a solution
    public void addXP(int amount, IGuild guild) {
        progress.addXP(amount, guild, this);
    }    
    
    public void prestige(IGuild guild) {
        progress.prestige(guild, this);
    }
}
