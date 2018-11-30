package discord.object;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class User {
    
    private final long discordID;
    private final Name name;
    private String desc;
    private final Progress progress;  
    
    @JsonCreator
    public User(@JsonProperty("discordID") long discordID, 
            @JsonProperty("name") Name name,
            @JsonProperty("desc") String desc,
            @JsonProperty("progress") Progress progress) {
        this.discordID = discordID;
        this.name = name;
        name.setUser(this);
        this.progress = progress;
        progress.setUser(this);
        this.desc = desc;
    }
          
    public User(long discordID, String name) {
        this.discordID = discordID;
        this.name = new Name(name);
        this.name.setUser(this);
        this.desc = "";
        this.progress = new Progress();
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
    
    public void setDesc(String desc) {
        this.desc = desc;
    }
  
    public boolean hasUnlocked(Unlockable unlockable) {
        return progress.getTotalLevel() >= unlockable.getTotalLevelRequired();
    }
}
