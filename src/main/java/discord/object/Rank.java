package discord.object;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

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
    
    public boolean equals(Rank other) {
        return (other.getName().equals(this.getName()) 
                && other.getLevelRequired() == this.levelRequired);
    }
}
