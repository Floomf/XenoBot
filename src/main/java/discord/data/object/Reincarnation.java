package discord.data.object;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

//Immutable
public class Reincarnation {
    
    private static final String[] KANJI = {"", "安心", "莫大", "超越", "道理"};
    private static final String[] ENGLISH = {"", "Anshin", "Bakudai", "Chouetsu", "Douri"};
    
    private final int number;
    
    @JsonCreator
    public Reincarnation(@JsonProperty("number") int number) {
        this.number = number;
    }
    
    public int getNumber() {
        return number;
    }
    
    @JsonIgnore
    public String getKanji() {
        return KANJI[number];
    }   
    
    @JsonIgnore
    public String getEnglish() {
        return ENGLISH[number];
    }
    
    @JsonIgnore
    public boolean isReincarnated() {
        return number > 0;
    }
    
    protected Reincarnation reincarnate() {
        return new Reincarnation(number + 1);
    }
    
    
}
