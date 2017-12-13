package discord.objects;

import discord.BotUtils;

public class User {
    
    private final long id; //Discord long id
    private final String name;
    private int level;
    private int xp;
    private int xpforLevel;
    private int balance;
    private Rank rank;

    public User() {
        this.id = 0;   
        this.name = "";
    }
 
    public User(long id, String name, Rank rank) {
        this.id = id;
        this.name = name;
        this.rank = rank;
        level = 1;
        xp = 0;
        balance = 100;
        genXPForLevel();
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
    
    public int getBalance() {
        return balance;
    }  
    
    public Rank getRank() {
        return rank;
    }
    
    public void setRank(Rank rank) {
        this.rank = rank;
    }
    
    //Mutators
    
    public void addLevels(int amount) {
        level += amount;
        genXPForLevel();          
    }
    
    public void addXP(int amount) {
        xp += amount;
    }
    
    public void addBalance(int amount) {
        balance += amount;
    }
    
    public void genXPForLevel() {
        xpforLevel = BotUtils.XP_MULTIPLIER * (level * 35 + 55);
    }
    
}
