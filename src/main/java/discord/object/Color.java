package discord.object;

public class Color {
    
    private final String name;
    private final int prestigeRequired;
    private final int levelRequired;
    
    public Color(String name, int prestige, int level) {
        this.name = name;
        this.prestigeRequired = prestige;
        this.levelRequired = level;
    }
    
    public String getName() {
        return name;
    }
    
    public int getTotalLevelsRequired() {
        return prestigeRequired * 80 + levelRequired;
    }
    
    @Override
    public String toString() {
        return getName();
    }
    
}
