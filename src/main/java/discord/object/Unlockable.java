package discord.object;

public class Unlockable {
    
    private final String name;
    private final int prestigeRequired;
    private final int levelRequired;
    
    public Unlockable(String name, int prestige, int level) {
        this.name = name;
        this.prestigeRequired = prestige;
        this.levelRequired = level;
    }
    
    public int getTotalLevelsRequired() {
        return prestigeRequired * Progress.MAX_LEVEL + levelRequired;
    }
    
    @Override
    public String toString() {
        return name;
    }
    
}
