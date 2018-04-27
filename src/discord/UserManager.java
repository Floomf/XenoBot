package discord;

import discord.objects.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

public class UserManager {
    
    private static List<User> users;
        
    //Database management
    
    public static void createDatabase(IGuild guild) {
        if (new File("users.json").exists()) {            
            loadDatabase();
            checkNewUsersInGuild(guild);
            checkRemovedUsersInGuild(guild);
        } else {
            System.out.println("Creating database...");
            users = new ArrayList<>();
            List<IUser> allUsers = guild.getUsers();
            allUsers.removeIf(user -> user.isBot());
            for (IUser user : allUsers) {
                users.add(new User(user.getLongID(), 
                        user.getDisplayName(guild), RankManager.getRankForLevel(1)));
            }
            System.out.println("Database created.");
            saveDatabase();
        }
        validateUsers(guild);
    }
    
    private static void loadDatabase() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            System.out.println("Loading database...");
            users = new ArrayList<>(Arrays.asList(
                    mapper.readValue(new File("users.json"), User[].class)));
            System.out.println("Database loaded.");                    
        } catch (IOException ex) {
            System.out.println(ex);
            System.out.println("Database failed to load.");
            System.exit(0);
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
    
    public static void validateUsers(IGuild guild) {
        for (User user : users) {
            LevelManager.setUserXPForLevel(user);
            NameManager.formatNameOfUser(guild, user);
            RankManager.setRankOfUser(guild, user);
        }
    }
    
    //Check possible users that have left the guild, if so, remove them from the database
    public static void checkRemovedUsersInGuild(IGuild guild) {
        System.out.println("Checking possible new users to remove from the database...");   
        
        for (User user : new ArrayList<>(users)) {
            //if id is null they no cant be found on the guild
            if (user.getLevel() < 10 && guild.getUserByID(user.getID()) == null) {
                users.remove(user);
                System.out.println("Removed " + user.getName() + " from the database.");
            }
        }
        System.out.println("Finished checking possible users to remove.");
    }
    
    //Check possible users that are not already in database in guild, add them if found
    private static void checkNewUsersInGuild(IGuild guild) {
        List<IUser> guildUsers = guild.getUsers();
        boolean foundNewUser = false;
        
        System.out.println("Checking possible new users to add to the database...");
        
        for (IUser dUser : guildUsers) {
            if (!dUser.isBot() && !databaseContainsUser(dUser)) {
                addUserToDatabase(dUser, guild);
                foundNewUser = true;
            }
        }
        System.out.println("Finished checking possible new guild users.");
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
        saveDatabase();
    }
    
    private static boolean databaseContainsUser(IUser dUser) {
        long ID = dUser.getLongID();
        for (User user : users) {
            if (ID == user.getID())
                return true;
        }
        return false;
    }
    
    public static boolean databaseContainsName(String name) {
        for (User user : users) {
            if (user.getName().toLowerCase().equals(name.toLowerCase()))
                return true;
        }
         return false;
    }
           
    //Methods for viewing/modifying user data
    
    public static User getUserFromID(long id) {
        for (User user : users) {
            if (user.getID() == id) 
                return user;
        }
        return null;
    }    
    
    public static long getUserIDFromName(String name) {
        for (User user : users) {
            if (user.getName().toLowerCase().equals(name.toLowerCase())) {
                return user.getID();
            }
        }
        return -1;
    }
    
    public static String getUserName(long id) {
        return getUserFromID(id).getName();
    }
    
    public static int getUserLevel(long id) {
        return getUserFromID(id).getLevel();
    }
    
    public static int getUserXP(long id) {
        return getUserFromID(id).getXP();
    }
    
    public static int getUserXPNeeded(long id) {
        return getUserFromID(id).getXPForLevel();
    }

}
