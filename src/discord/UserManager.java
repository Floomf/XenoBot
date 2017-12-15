package discord;

import discord.objects.Rank;
import discord.objects.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

public class UserManager {
    
    private static List<User> users;
        
    //Database management
    
    public static void createDatabase(IGuild guild) {
        if (new File("users.json").exists()) {            
            loadDatabase();
            checkUsersInGuild(guild);
        } else {
            System.out.println("Creating database...");
            users = new ArrayList<User>();
            List<IUser> allUsers = guild.getUsers();
            allUsers.removeIf(user -> user.isBot());
            for (IUser user : allUsers) {
                users.add(new User(user.getLongID(), 
                        user.getDisplayName(guild), RankManager.getRankForLevel(1)));
            }
            System.out.println("Database created.");
            saveDatabase();
            RankManager.setRanksForGuild(guild, users);
        }
        NameManager.formatAllNames(guild, users);
        RankManager.setRanksForGuild(guild, users);
    }
    
    private static void loadDatabase() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            System.out.println("Loading database...");
            users = new ArrayList<>(Arrays.asList(
                    mapper.readValue(new File("users.json"), User[].class)));
            System.out.println("Database loaded.");
            
            for (User user : users) {
                user.genXPForLevel();
            }
            
        } catch (IOException ex) {
            System.out.println(ex);
            System.out.println("Database failed to load.");
        }
    }
    
    public static void saveDatabase() {
        ObjectMapper mapper = new ObjectMapper();
        
        System.out.println("Attempting to save database...");
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File("users.json"), users);
            System.out.println("Database saved.");
        } catch (IOException ex) {
            System.out.println(ex);
            System.out.println("Database failed to save.");
        }
    }
    
    //Check possible users that are not already in database in guild, add them if found
    private static void checkUsersInGuild(IGuild guild) {
        List<IUser> guildUsers = guild.getUsers();
        boolean foundNewUser = false;
        
        System.out.println("Checking possible new users to add to the database...");
        
        for (IUser dUser : guildUsers) {
            if (!dUser.isBot() && !databaseContainsUser(dUser)) {
                addUserToDatabase(dUser, guild);
                foundNewUser = true;
            }
        }
        System.out.println("Finished checking guild users.");
        if (foundNewUser) 
            saveDatabase();
    }
    
    public static void addUserToDatabase(IUser dUser, IGuild guild) {
        User user = new User(dUser.getLongID(), 
                dUser.getDisplayName(guild), RankManager.getRankForLevel(1));
        users.add(user);
        RankManager.setRankOfUser(guild, user);
        NameManager.formatNameOfUser(guild, user);
        System.out.println("Added " + user.getName() + " to the database.");
    }
    
    private static boolean databaseContainsUser(IUser dUser) {
        long ID = dUser.getLongID();
        for (User user : users) {
            if (ID == user.getID())
                return true;
        }
        return false;
    }
           
    //Methods for viewing/modifying user data
    
    private static User getUser(long id) {
        for (User user : users) {
            if (user.getID() == id) 
                return user;
        }
        return null;
    }    
    
    public static long getUserIDFromName(String name) {
        for (User user : users) {
            if (user.getName().toLowerCase().equals(name)) {
                return user.getID();
            }
        }
        return -1;
    }
    /*
    public static void test(IGuild guild) {
        for (User user : users) {
            int level = user.getLevel();
            for (int i = 1; i < level; i++) {
                user.addXP(-level * 5);
                if (checkLeveldownUser(user))
                    addLevelsUser(guild, user, -1);
            }
        }
    }*/
    
    public static String getUserName(long id) {
        return getUser(id).getName();
    }
    
    public static int getUserLevel(long id) {
        return getUser(id).getLevel();
    }
    
    public static int getUserXP(long id) {
        return getUser(id).getXP();
    }
    
    public static int getUserXPNeeded(long id) {
        return getUser(id).getXPForLevel();
    }
    
    public static int getUserBalance(long id) {
        return getUser(id).getBalance();
    }
    
    public static void addToUserBalance(long id, int amount) {
        getUser(id).addBalance(amount);
    }
    
    public static void addUserXP(IGuild guild, long id, int amount) {
        User user = getUser(id);
        user.addXP(amount); 
        if (checkLevelupUser(user))
            addLevelsUser(guild, user, 1);
        else if (checkLeveldownUser(user))
            addLevelsUser(guild, user, -1);
    }
    
    //this needs to be a lot cleaner, fix it sooner
    private static void addLevelsUser(IGuild guild, User user, int amount) {
        IChannel botsChannel = guild.getChannelByID(250084663618568192L);
        
        if (amount > 0) {
            user.addXP(-user.getXPForLevel()); //carry over xp to next level
        }
        
        user.addLevels(amount); 
        
        if (amount < 0) {
            user.addXP(user.getXPForLevel() + user.getXP()); //subtract from the max
        }      
        NameManager.formatNameOfUser(guild, user);
     
        BotUtils.sendMessage(botsChannel, guild.getUserByID(user.getID()).mention() 
                + "```Level up! You are now level " + user.getLevel() + ".```"); 
        
        Rank rankNeeded = RankManager.getRankForLevel(user.getLevel());
        if (!user.getRank().equals(rankNeeded)) {         
            user.setRank(rankNeeded);
            RankManager.setRankOfUser(guild, user);
            BotUtils.sendMessage(botsChannel,
                    "```Congratulations! You are now (a/an) " + rankNeeded.getName() + ".```");
        }
        
        if (checkLevelupUser(user)) 
            addLevelsUser(guild, user, 1);
        if (checkLeveldownUser(user))
            addLevelsUser(guild, user, -1);
    }
    
    private static boolean checkLevelupUser(User user) {
        return (user.getXP() >= user.getXPForLevel());
    }
    
    private static boolean checkLeveldownUser(User user) {
        return (user.getXP() < 0);
    }
      
}
