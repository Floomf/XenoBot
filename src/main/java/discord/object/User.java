package discord.object;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class User {
    
    @JsonProperty("discordID")
    private final long discordID;
    private Name name;
    private Progress progress;  
    
    @JsonCreator
    public User(@JsonProperty("discordID") long discordID, 
            @JsonProperty("name") Name name, 
            @JsonProperty("progress") Progress progress) {
        this.discordID = discordID;
        this.name = name;
        name.setUser(this);
        this.progress = progress;
        progress.setUser(this);
;    }
          
    public User(long discordID, String name) {
        this.discordID = discordID;
        this.name = new Name(name);
        this.name.setUser(this);
        this.progress = new Progress();
        this.progress.setUser(this);
    }
    
    //Accessors   
    public long getID() {
        return discordID;
    }
    
    public Progress getProgress() {
        return progress;
    }
    
    public Name getName() {
        return name;
    }
  
    public boolean hasUnlocked(Unlockable unlockable) {
        return progress.getTotalLevels() >= unlockable.getTotalLevelRequired();
    }
}
