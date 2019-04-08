package discord.data.object;

//Immutable
public class Unlockable {
    
    private final String name;
    private final int totalLevelRequired;
    
    public Unlockable(String name, int prestige, int level) {
        this.name = name;
        this.totalLevelRequired = prestige * Progress.MAX_LEVEL + level;
    }
    
    public int getTotalLevelRequired() {
        return totalLevelRequired;
    }
    
    @Override
    public String toString() {
        return name;
    }
    
}
