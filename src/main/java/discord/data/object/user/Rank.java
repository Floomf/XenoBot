package discord.data.object.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

//Immutable
public class Rank {

    protected static Rank[] RANKS;

    static {
        ObjectMapper mapper = new ObjectMapper();
        try {
            RANKS = mapper.readValue(new File("ranks.json"), Rank[].class);
            System.out.println(RANKS.length + " ranks loaded.");
        } catch (IOException e) {
            System.out.print("Could not load ranks with error: " + e);
        }
    }

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

    public static Rank getRankForLevel(int level) {
        for (int i = 0; i < RANKS.length; i++) {
            if (level < RANKS[i].getLevelRequired()) {
                return RANKS[i - 1];
            }
        }
        return RANKS[RANKS.length - 1]; //has to be last rank
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
    public static Rank getMaxRank() {
        return RANKS[RANKS.length - 1];
    }

    @JsonIgnore
    public boolean isMax() {
        return this.equals(getMaxRank());
    }

}
