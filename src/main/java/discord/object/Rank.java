package discord.object;

public class Rank {
    
    private final String name;
    private final int levelRequired;
    
    private Rank() {
        this.name = "";
        this.levelRequired = 0;
    }
    
    public Rank(String name, int levelRequired) {
        this.name = name;
        this.levelRequired = levelRequired;
    }
    
    public String getName() {
        return name;
    }
    
    public int getLevelRequired() {
        return levelRequired;
    }
    
    public boolean equals(Rank other) {
        return (other.getName().equals(this.getName()) 
                && other.getLevelRequired() == this.levelRequired);
    }
}
