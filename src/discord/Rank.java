package discord;

public class Rank {
    
    private final long id;
    private final String name;
    private final int levelRequired;
    
    private Rank() {
        this.id = 0;
        this.name = "";
        this.levelRequired = 0;
    }
    
    public Rank(long ID, String name, int levelRequired) {
        this.id = ID;
        this.name = name;
        this.levelRequired = levelRequired;
    }
    
    public long getID() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public int getLevelRequired() {
        return levelRequired;
    }
    
    public boolean equals(Rank other) {
        return (other.getID() == this.getID());
    }
}
