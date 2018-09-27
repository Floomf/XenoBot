package discord.object;

//Immutable class

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;


public class Prestige {
    
    private static final int MAX_PRESTIGE = 10;
    public static final char[] BADGES = {0, '★','✷','⁂','❖','❃','✠','✪','☭','☬','♆'};
    
    private final int number;  
    
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
   
    public Prestige prestige() {
        return new Prestige(number + 1);
    }
    
}
