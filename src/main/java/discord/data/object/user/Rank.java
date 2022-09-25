package discord.data.object.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import discord4j.core.object.entity.Role;

import java.io.File;
import java.io.IOException;

//Immutable
public class Rank {

    public static Rank[] RANKS;

    static {
        ObjectMapper mapper = new ObjectMapper();
        try {
            RANKS = mapper.readValue(new File("ranks.json"), Rank[].class);
            System.out.println(RANKS.length + " ranks loaded.");
        } catch (IOException e) {
            System.out.print("Could not load ranks with error: " + e);
        }
    }

    private String name;
    private final int levelRequired;

    @JsonCreator
    public Rank(@JsonProperty("name") String name,
                @JsonProperty("levelRequired") int levelRequired) {
        this.name = name;
        this.levelRequired = levelRequired;
    }

    public static void saveRanks() {
        try {
            System.out.println("Saving ranks...");
            new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(new File("ranks.json"), RANKS);
        } catch (IOException e) {
            System.out.print("Could not save ranks with error: " + e);
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public static Rank getRankForLevel(int level) {
        for (int i = 0; i < RANKS.length; i++) {
            if (level < RANKS[i].getLevelRequired()) {
                return RANKS[i - 1];
            }
        }
        return RANKS[RANKS.length - 1]; //has to be last rank
    }

    @JsonIgnore
    public static Rank getMaxRank() {
        return RANKS[RANKS.length - 1];
    }

    public String getName() {
        return name;
    }

    public int getLevelRequired() {
        return levelRequired;
    }

    @JsonIgnore
    public static boolean isRankRole(Role role) {
        for (Rank rank : RANKS) {
            if (role.getName().equals(rank.getName())) {
                return true;
            }
        }
        return false;
    }

    @JsonIgnore
    public boolean isMax() {
        return this.equals(getMaxRank());
    }

}
