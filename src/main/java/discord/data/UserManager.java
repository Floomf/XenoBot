package discord.data;

import discord.data.object.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import discord.util.BotUtils;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class UserManager {

    private static HashMap<Long, User> users;
    
    public static Collection<User> getUsers() {
        return users.values();
    }

    public static void createDatabase(IGuild guild) {
        users = new HashMap<>();
        if (new File("users.json").exists()) {
            loadDatabase();
            checkNewUsersInGuild(guild);
            checkRemovedUsersInGuild(guild);
        } else {
            System.out.println("Creating database...");
            List<IUser> allUsers = guild.getUsers();
            allUsers.removeIf(user -> user.isBot());
            allUsers.forEach(user -> {
                users.put(user.getLongID(), new User(user.getLongID(), user.getDisplayName(guild)));
            });
            System.out.println("Database created.");
        }
        validateUsers(guild);
        saveDatabase();
    }

    private static void loadDatabase() {
        ObjectMapper mapper = new ObjectMapper();
        //mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            System.out.println("Loading database...");
            User[] usersToLoad = mapper.readValue(new File("users.json"), User[].class);
            for (User user : usersToLoad) {
                users.put(user.getDiscordID(), user);
            }
            System.out.println("Database loaded.");
        } catch (IOException e) {
            System.err.println("Database failed to load with error: " + e);
        }
    }

    public static void saveDatabase() {
        ObjectMapper mapper = new ObjectMapper();
        System.out.println("Attempting to save database...");
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File("users.json"), users.values());
            System.out.println("Database saved.");
        } catch (IOException e) {
            System.err.println("Database failed to save with error: " + e);
        }
    }

    public static void validateUsers(IGuild guild) {
        for (User user : users.values()) {
            if (guild.getUserByID(user.getDiscordID()) != null) {
                RankManager.verifyRoleOnGuild(guild, user);
                user.getName().verify(guild);
            }
        }
    }

    //Check possible users that have left the guild, if so, remove them from the database
    public static void checkRemovedUsersInGuild(IGuild guild) {
        System.out.println("Checking any possible guild users (that left) to remove from the database...");
        users.values().removeIf(user -> userIsInvalid(user, guild)); //Won't log when a user is removed, just does it silently
        System.out.println("Removed any possible users that were invalid");
    }

    //Check possible users that are not already in database in guild, add them if found
    private static void checkNewUsersInGuild(IGuild guild) {
        List<IUser> guildUsers = guild.getUsers();

        System.out.println("Checking possible newly joined guild users to add to the database...");
        for (IUser dUser : guildUsers) {
            if (!dUser.isBot() && !databaseContainsDUser(dUser)) {
                handleUserJoin(dUser, guild);
            }
        }
        System.out.println("Finished checking possible new guild users.");
    }

    public static void handleUserJoin(IUser dUser, IGuild guild) {
        if (databaseContainsDUser(dUser)) {
            User existingUser = getDBUserFromDUser(dUser);
            RankManager.verifyRoleOnGuild(guild, existingUser);
            existingUser.getName().verify(guild);
            System.out.println("User " + existingUser.getName().getNick() + " joined. Already found them saved in the database.");
        } else { //They're not in the database, so we add the new user in
            String name = BotUtils.validateNick(dUser.getDisplayName(guild));
            //if the name validator returns an empty name, we need a placeholder
            if (name.isEmpty()) {
                name = "Realmer";
            }
            User user = new User(dUser.getLongID(), name);
            users.put(dUser.getLongID(), user);
            RankManager.verifyRoleOnGuild(guild, user);
            user.getName().verify(guild);
            System.out.println("User " + name + " joined. Added them to the database.");
            saveDatabase();
        }
    }

    public static void handleUserLeave(IUser dUser, IGuild guild) {
        if (databaseContainsDUser(dUser)) {
            removeUserIfInvalid(getDBUserFromDUser(dUser), guild);
        } else {
            System.out.println("User " + dUser.getName() + " left the guild, but they weren't in the database anyway.");
        }
    }

    private static void removeUserIfInvalid(User user, IGuild guild) {
        //we keep users that are level 10 and up for now
        if (userIsInvalid(user, guild)) {
            users.remove(user.getDiscordID());
            System.out.println("Removed " + user.getName() + " from the database.");
        }
    }

    private static boolean userIsInvalid(User user, IGuild guild) {
        return (user.getProgress().getTotalLevel() < 15 && guild.getUserByID(user.getDiscordID()) == null);
    }

    //Methods for fetching users
    public static User getDBUserFromID(long id) {
        return users.get(id);
    }

    public static User getDBUserFromDUser(IUser dUser) {
        return getDBUserFromID(dUser.getLongID());
    }

    public static User getDBUserFromMessage(IMessage message) {
        return getDBUserFromID(message.getAuthor().getLongID());
    }

    public static boolean databaseContainsDUser(IUser dUser) {
        return users.containsKey(dUser.getLongID());
    }

}
