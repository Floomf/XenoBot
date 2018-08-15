package discord.objects;

public class Color {
    
    private String name;
    private int prestigeRequired;
    private int levelRequired;
    
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
    
    public String toString() {
        return getName();
    }
    
}
