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
                RankManager.verifyRankOnGuild(guild, user);
                user.getName().verify(guild);
            }
        }
    }
    
    //Check possible users that have left the guild, if so, remove them from the database
    public static void checkRemovedUsersInGuild(IGuild guild) {
        System.out.println("Checking possible new users to remove from the database...");          
        for (User user : new ArrayList<>(users)) {
           removeUserIfInvalid(user, guild);
        }
        System.out.println("Finished checking possible users to remove.");
    }
    
    //Check possible users that are not already in database in guild, add them if found
    private static void checkNewUsersInGuild(IGuild guild) {
        List<IUser> guildUsers = guild.getUsers();
        boolean foundNewUser = false;
        
        System.out.println("Checking possible new users to add to the database...");
        
        for (IUser dUser : guildUsers) {
            if (!dUser.isBot() && !databaseContainsDUser(dUser)) {
                handleUserJoin(dUser, guild);
                foundNewUser = true;
            }
        }
        System.out.println("Finished checking possible new guild users.");
        if (foundNewUser) 
            saveDatabase();
    }
    
    public static void handleUserJoin(IUser dUser, IGuild guild) {
        if (databaseContainsDUser(dUser)) {
            User existingUser = getDBUserFromDUser(dUser);
            RankManager.verifyRankOnGuild(guild, existingUser);
            existingUser.getName().verify(guild);
            System.out.println("Already found " + existingUser.getName().getNick() + " in the database.");
            return;
        }
        String name = BotUtils.validateName(dUser.getDisplayName(guild));
        //if the name validator returns an empty name, we need a placeholder
        if (name.isEmpty())  {
            name = "User";
        }
        if (databaseContainsName(name)) {
            name = getNextAvailableName(name);
        }
        User user = new User(dUser.getLongID(), name);
        users.add(user);
        RankManager.verifyRankOnGuild(guild, user);
        user.getName().verify(guild);
        System.out.println("Added " + name + " to the database.");
        saveDatabase();
    }
    
    private static String getNextAvailableName(String name) {
        int i = 2;
        while (databaseContainsName(name + i)) i++;
        return name + i;
    }
    
    public static void handleUserLeave(IUser dUser, IGuild guild) {
        //here we are assuming the dUser is in the DB, seems like a bad idea
        removeUserIfInvalid(getDBUserFromDUser(dUser), guild);
    }
    
    private static void removeUserIfInvalid(User user, IGuild guild) {
        //we keep users that are level 10 and up for now
        if (user.getProgress().getTotalLevels() < 10 && guild.getUserByID(user.getID()) == null) {
            users.remove(user);
            System.out.println("Removed " + user.getName() + " from the database.");
        }
    }
    
    private static boolean databaseContainsDUser(IUser dUser) {
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
           
    //Methods for fetching users and ids
    
    public static User getDBUserFromID(long id) {
        for (User user : users) {
            if (user.getID() == id) 
                return user;
        }
        return null;
    }
    
    public static User getDBUserFromDUser(IUser dUser) {
        return getDBUserFromID(dUser.getLongID());
    }
    
    public static User getDBUserFromMessage(IMessage message) {
        return getDBUserFromID(message.getAuthor().getLongID());
    }
    
    public static long getDBUserIDFromName(String name) {
        for (User user : users) {
            if (user.getName().getNick().equalsIgnoreCase(name)) {
                return user.getID();
            }
        }
        return -1;
    }
    
}
