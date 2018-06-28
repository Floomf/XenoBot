package discord.objects;

public class User {
    
    private final long id; //Discord long id
    private String name;
    private int level;
    private int xp;
    private int xpforLevel;
    private int prestige;
    private int emojicp;
    private Rank rank;
    
    public User() {
        this.id = 0;   
    }
    
    public User(long id, String name, Rank rank) {
        this.id = id;
        this.name = name;
        this.rank = rank;
        level = 1;
        xp = 0;
        prestige = 0;
        emojicp = 0;
    }
    
    //Accessors
    
    public long getID() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public int getLevel() {
        return level;
    }
    
    public int getXP() {
        return xp;
    }
    
    public int getXPForLevel() {
        return xpforLevel;
    }
    
    public int getPrestige() {
        return prestige;
    }
    
    public Rank getRank() {
        return rank;
    }
    
    public int getEmoji() {
        return emojicp;
    }
  
    //Mutators
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setRank(Rank rank) {
        this.rank = rank;
    }
    
    public void setXPForLevel(int xpforLevel) {
        this.xpforLevel = xpforLevel;
    }
    
    public void setXP(int xp) {
        this.xp = xp;
    }
    
    public void setLevel(int level) {
        this.level = level;
    }
    
     public void setEmoji(int codepoint) {
        emojicp = codepoint;
    }
     
    public void addLevels(int amount) {
        level += amount;         
    }
    
    public void addXP(int amount) {
        xp += amount;
    }    
    
    public void addPrestige() {
        this.prestige++;
    }
}
