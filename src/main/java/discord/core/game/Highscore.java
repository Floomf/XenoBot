package discord.core.game;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Highscore {

    private long discordID;
    private int highscore;

    //@JsonCreator
    public Highscore(@JsonProperty("discordID") long discordID, @JsonProperty("highscore") int highscore) {
        this.discordID = discordID;
        this.highscore = highscore;
    }

    public long getDiscordID() {
        return discordID;
    }

    public int getHighscore() {
        return highscore;
    }

    public boolean validateNewScore(int score) {
        if (score > highscore) {
            highscore = score;
            return true;
        }
        return false;
    }

}
