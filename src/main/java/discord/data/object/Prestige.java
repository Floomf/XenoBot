package discord.data.object;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

//Immutable
public class Prestige {
    
    public static final int MAX_PRESTIGE = 10;
    public static final char[] BADGES = {0,'★','✷','⁂','❖','✪','❃','❈','✠','♆','֎'};
    
    private final int number;
    
    @JsonCreator
    public Prestige(@JsonProperty("number") int number) {
        this.number = number;
    }
    
    @JsonIgnore
    public char getBadge() {
        return BADGES[number];
    }
    
    public int getNumber() {
        return number;
    }
    
    @JsonIgnore
    public boolean isPrestiged() {
        return number > 0;
    }
    
    @JsonIgnore
    public boolean isMax() {
        return number == MAX_PRESTIGE;
    }
    
    protected Prestige prestige() {
        return new Prestige(number + 1);
    }
    
}
