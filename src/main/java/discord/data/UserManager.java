package discord.data;

import com.fasterxml.jackson.databind.DeserializationFeature;
import discord.data.object.user.DUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import discord.util.BotUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import discord4j.core.event.domain.guild.MemberJoinEvent;
import discord4j.core.event.domain.guild.MemberLeaveEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Mono;

public class UserManager {

    //we can't map member to DUser because some members can't be retrieved if they aren't on the guild
    private static HashMap<Long, DUser> dUsers;

    public static Collection<DUser> getDUsers() {
        return dUsers.values();
    }

    public static void createDatabase(Guild guild) {
        dUsers = new HashMap<>();
        if (new File("users.json").exists()) {
            loadDatabase(guild);
            checkNewUsersInGuild(guild);
        } else {
            System.out.println("Creating database...");
            List<Member> allMembers = guild.getMembers().collectList().block();
            allMembers.removeIf(Member::isBot);
            allMembers.forEach(UserManager::addMemberToDB);
            System.out.println("Database created.");
        }
        saveDatabase();
    }

    private static void loadDatabase(Guild guild) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            System.out.println("Loading database...");
            DUser[] dUsersToLoad = mapper.readValue(new File("users.json"), DUser[].class);
            for (DUser user : dUsersToLoad) {
                dUsers.put(user.getDiscordID(), user);
            }

            //setting DUsers Member field and removing invalid users
            for (DUser dUser : dUsersToLoad) {
                //Only way I know to return null for member not in guild (like the old code)
                Member member = guild.getMemberById(Snowflake.of(dUser.getDiscordID())).onErrorResume(e -> Mono.empty()).block();
                if (member == null) {
                    if (dUser.getProg().getLevel() <= 15) {
                        dUsers.remove(dUser.getDiscordID());
                        System.out.println("Removed " + dUser.getName() + " from the database.");
                    }
                    continue;
                }

                dUser.setGuildMember(member);
                dUser.verifyOnGuild();
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
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File("users.json"), dUsers.values());
            System.out.println("Database saved.");
        } catch (IOException e) {
            System.err.println("Database failed to save with error: " + e);
        }
    }

    //Check possible users that are not already in database in guild, add them if found
    private static void checkNewUsersInGuild(Guild guild) {
        List<Member> guildMembers = guild.getMembers().collectList().block();

        System.out.println("Checking any newly joined guild users to add to the database...");
        for (Member member : guildMembers) {
            if (member.isBot() || databaseContainsMember(member)) {
                continue;
            }
            addMemberToDB(member);
        }
        System.out.println("Finished checking possible new guild users.");
    }


    private static void addMemberToDB(Member member) {
        String name = BotUtils.validateNick(member.getDisplayName());
        //if the name validator returns an empty name, we need a placeholder
        if (name.isEmpty()) {
            name = "Realmer";
        }
        DUser dUser = new DUser(member, name);
        dUsers.put(member.getId().asLong(), dUser);

        System.out.println("Added " + member.getDisplayName() + " to the database.");
    }

    public static void onMemberJoinEvent(MemberJoinEvent event) {
        System.out.println(event.getMember().getDisplayName() + " joined the guild.");
        Member member = event.getMember();
        if (!member.isBot()) {
            if (databaseContainsMember(member)) {
                DUser existingUser = getDUserFromMember(member);
                existingUser.setGuildMember(member);
                existingUser.verifyOnGuild();
                System.out.println("Already found " + event.getMember().getDisplayName() + " in the database.");
            } else { //They're not in the database, so we add the new user in
                addMemberToDB(member);
                saveDatabase();
            }
        }
    }

    public static void onMemberLeaveEvent(MemberLeaveEvent event) {
        System.out.println("Member " + event.getUser().getUsername() + " left the guild.");
        if (!event.getUser().isBot()) {
            DUser dUser = dUsers.get(event.getUser().getId().asLong());
            if (dUser.getProg().getTotalLevel() <= 15) {
                dUsers.remove(event.getUser().getId().asLong());
                System.out.println("Removed " + event.getUser().getUsername() + " from the database.");
                saveDatabase();
            }
        }
    }

    //Methods for fetching users
    public static DUser getDUserFromID(long id) {
        return dUsers.get(id);
    }

    public static DUser getDUserFromUser(User user) {
        return dUsers.get(user.getId().asLong());
    }

    public static DUser getDUserFromMember(Member member) {
        return dUsers.get(member.getId().asLong());
    }

    public static DUser getDUserFromMessage(Message message) {
        return dUsers.get(message.getAuthor().get().getId().asLong()); //uh oh
    }

    public static int size() {
        return dUsers.size();
    }

    public static boolean databaseContainsUser(User user) {
        return dUsers.containsKey(user.getId().asLong());
    }

    private static boolean databaseContainsMember(Member member) {
        return dUsers.containsKey(member.getId().asLong());
    }

}
