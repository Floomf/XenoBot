package discord.data.object;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import discord.data.RankManager;

//Immutable
public class Rank {
    
    private final String name;
    private final String roleName;
    private final int levelRequired;
    
    @JsonCreator
    public Rank(@JsonProperty("name") String name,
            @JsonProperty("roleName") String roleName,
            @JsonProperty("levelRequired") int levelRequired) {
        this.name = name;
        this.roleName = roleName;
        this.levelRequired = levelRequired;
    }
    
    public String getName() {
        return name;
    }
    
    public String getRoleName() {
        return roleName;
    }
    
    public int getLevelRequired() {
        return levelRequired;
    }
    
    @JsonIgnore
    public boolean isMax() {
        return this.equals(RankManager.getMaxRank());
    }
    
}
