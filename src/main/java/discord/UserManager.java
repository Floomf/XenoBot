package discord;

import com.fasterxml.jackson.databind.DeserializationFeature;
import discord.object.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
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
                users.add(new User(user.getLongID(), user.getDisplayName(guild)));
            }
            System.out.println("Database created.");
            saveDatabase();
        }
        validateUsers(guild);
    }
    
    private static void loadDatabase() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            System.out.println("Loading database...");         
            users = new ArrayList<>(Arrays.asList(
                    mapper.readValue(new File("users.json"), User[].class)));
            System.out.println("Database loaded.");                    
        } catch (IOException e) {
            System.err.println("Database failed to load with error: " + e);
        }
    }
    
    public static void saveDatabase() {
        ObjectMapper mapper = new ObjectMapper();
        
        System.out.println("Attempting to save database...");
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File("users.json"), users);
            System.out.println("Database saved.");
        } catch (IOException e) {
            System.err.println("Database failed to save with error: ");
            e.printStackTrace();
        }
    }
    
    public static void validateUsers(IGuild guild) {
        for (User user : users) {
            if (guild.getUserByID(user.getID()) != null) {
                RankManager.setRankOfUser(guild, user);
                user.getName().verify(guild);
            }
        }
    }
    
    //Check possible users that have left the guild, if so, remove them from the database
    public static void checkRemovedUsersInGuild(IGuild guild) {
        System.out.println("Checking possible new users to remove from the database...");   
        
        for (User user : new ArrayList<>(users)) {
            //if id is null then they cant be found on the guild
            //but only remove them if they are low enough level
            if (user.getProgress().getTotalLevels() < 10 && guild.getUserByID(user.getID()) == null) {
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
        User user = new User(dUser.getLongID(), dUser.getDisplayName(guild));
        users.add(user);
        RankManager.setRankOfUser(guild, user);
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
            if (user.getName().getNick().toLowerCase().equals(name.toLowerCase()))
                return true;
        }
         return false;
    }
           
    //Methods for viewing/modifying user data
    
    public static User getDBUserFromID(long id) {
        for (User user : users) {
            if (user.getID() == id) 
                return user;
        }
        return null;
    }    
    
    public static long getDBUserIDFromName(String name) {
        for (User user : users) {
            if (user.getName().getNick().toLowerCase().equals(name.toLowerCase())) {
                return user.getID();
            }
        }
        return -1;
    }
    
    public static User getDBUserFromMessage(IMessage message) {
        return getDBUserFromID(message.getAuthor().getLongID());
    }
}
