package discord;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.*;
import java.util.List;
import java.util.ArrayList;
import sx.blah.discord.api.events.EventSubscriber;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

import java.util.concurrent.TimeUnit;
import org.apache.commons.io.FileUtils;

public class CommandHandler {

    public enum PoolType {
        ALL, ONLINE, OFFLINE, VOICE
    }

    private List<IRole> colorRoles;

    public List<IUser> getUsers(PoolType pool, IGuild guild, IMessage message) {
        List<IUser> users = guild.getUsers();    
        if (pool == PoolType.ONLINE) {
            users.removeIf(user -> user.getPresence().getStatus().equals(StatusType.OFFLINE));
        } else if (pool == PoolType.OFFLINE) {
            //test this
            users.removeIf(user -> !user.getPresence().getStatus().equals(StatusType.OFFLINE));
        } else if (pool == PoolType.VOICE) {
            users = message.getAuthor().getVoiceStateForGuild(guild).getChannel().getConnectedUsers();
        }
        users.removeIf(user -> user.isBot());
        return users;
    }

    //TO RE-ADD
    /*public String listRolesByName() {
        StringBuilder sb = new StringBuilder();
        for (IRole role : colorRoles) {
            sb.append(role.getName());
            sb.append(", ");
        }
        sb.delete(sb.length() - 2, sb.length());
        return sb.toString();
    }*/

    @EventSubscriber
    public void onMessageEvent(MessageReceivedEvent event) {
        IMessage message = event.getMessage();
        IUser user = message.getAuthor();
        long id = user.getLongID();
        IGuild guild = message.getGuild();
        IChannel channel = message.getChannel();

        //seperate the contents of the message into an array of strings
        String[] args = message.getContent().toLowerCase()
                .trim().replaceAll("\\s\\s+", " ").split("\\s");
        
        boolean hasArgs = (args.length > 1);
        boolean isOwner = user.equals(guild.getOwner());

        if (args.length == 0) {
            return;
        }

        //Make sure the message has the command prefix
        if (!args[0].startsWith(BotUtils.CMD_PREFIX)) {
            return;
        }

        //Make sure the guild has a bots channel
        if (guild.getChannelsByName("bots").isEmpty()) {
            BotUtils.sendInfoMessage(channel,
                    "Please create a new text channel named \"#bots\"!"
                    + " I will only function properly there. Beep boop.");
            return;
        }
        
        //Make sure command is in the bots channel
        if (!channel.getName().equals("bots")) {
            BotUtils.sendInfoMessage(channel,
                    "I only respond to commands within the \"#bots\" channel."
                    + " Please type your command again there.");
            return;
        }

        String commandStr = args[0].substring(1);

        switch (commandStr) {

            case "help":
                if (isOwner) {
                    BotUtils.sendMessage(channel, "Owner Commands", 
                            "!setnames   - Mass change users' nicknames."
                            + "\n!resetnames - Mass reset users' nicknames."
                            + "\n!flood      - Flood your connected voice channel."
                            + "\n!givexp     - Give XP to a specifed user.");
                }               
                BotUtils.sendMessage(channel, "User Commands", 
                        "!level   - View your or another user's level progress."
                        + "\n!balance - View your or another user's balance."
                        + "\n!rng     - Generate a random number."
                        + "\n!flip    - Flip a coin."
                        + "\n!cat     - See a random cat."
                        + "\n!dadjoke - Read a random dad joke."
                        + "\n!raffle  - Choose a random user."
                        + "\n!info    - View bot information.");
                return;

            //TO RE-ADD WITH LEVEL SYSTEM
            /*case "color":
                //Check if the command was sent in The Realm
                if (!(guild.getLongID() == 98236427971592192L)) {
                    BotUtils.sendMessage(channel, "**Info**```This command only functions on The Realm. Sorry about that.```");
                    return;
                }

                if (!hasArgs) {
                    BotUtils.sendMessage(channel, "**Usage** ```.color [name]\n\nChanges the color of your name.```"
                            + "\n**Available choices:**```" + listRolesByName() + "```");
                    return;
                    //Combine args for colors like "light blue"      
                } else if (args.length > 2) {
                    for (int i = 2; i <= args.length - 1; i++) {
                        args[1] += " " + args[i];
                    }
                }

                //Loop through roles and check for the color
                for (IRole color : colorRoles) {
                    if (args[1].equals(color.getName().toLowerCase())) {
                        guild.editUserRoles(user, new IRole[]{client.getRoleByID(275486798699036683L), color});
                        BotUtils.sendMessage(channel, 
                                String.format("```Your name is now %s!```", color.getName()));
                        return;
                    }
                }
                BotUtils.sendMessage(channel, "**Unknown color!** Available choices: ```"
                        + listRolesByName() + "```");
                return;*/

            case "rng":
                if (!hasArgs) {
                    BotUtils.sendUsageMessage(channel, "!rng [max]"
                            + "\n\nGenerates a integer from 1 to the max."
                            + "\nMax must be greater than 1.");
                    return;
                }

                try {
                    int limit = Integer.parseInt(args[1]);
                    if (limit > 1) {
                        BotUtils.sendMessage(channel, "Result", 
                                String.valueOf((int) (Math.random() * limit + 1)));
                        return;
                    }
                } catch (NumberFormatException e) {
                }
                BotUtils.sendErrorMessage(channel, "Parameter is not an integer greater than one.");
                return;

            case "flip":
                String result = "Heads";
                if (Math.random() < 0.5) {
                    result = "Tails";
                }
                BotUtils.sendMessage(channel, "Result", result);
                return;

            case "cat":
                if (!hasArgs) {
                    BotUtils.sendUsageMessage(channel, "!cat [pic/gif]"
                            + "\n\nView a random cat picture or gif.");
                    return;
                }

                if (args[1].equals("pic") || args[1].equals("gif")) {
                    String type = args[1];
                    if (type.equals("pic")) {
                        type = "png";
                    }
                    File f = new File("cat." + type);
                    try {
                        FileUtils.copyURLToFile(new URL(
                                "http://thecatapi.com/api/images/get?format=src&api_key=MjA2OTcy&type=" + type), f);
                        message.getChannel().sendFile(f);
                        return;
                    } catch (IOException ex) {
                    }
                }
                BotUtils.sendErrorMessage(channel, "Unknown media type. Type \"!cat\" for help.");
                return;

            case "dadjoke":
                try {
                    URL url = new URL("https://icanhazdadjoke.com/");
                    HttpURLConnection hc = (HttpURLConnection) url.openConnection();
                    hc.setRequestProperty("Accept", "text/plain");
                    hc.setRequestProperty("User-Agent", "Discord Bot");
                    hc.connect();
                    BufferedReader br = new BufferedReader(new InputStreamReader(hc.getInputStream()));
                    BotUtils.sendMessage(channel, String.format("```%s```", br.readLine()));
                    return;
                } catch (IOException ex) {
                    System.out.println(ex);
                }

            case "raffle":
                if (!hasArgs) {
                    BotUtils.sendUsageMessage(channel, 
                            "!raffle [all/online/offline/voice]"
                            + "\n\nChooses a random user from the selected pool."
                            + "\nThis does not include yourself or any bots."
                            + "\n\nall     - All server users."
                            + "\nonline  - All online users on the server."
                            + "\noffline - All offline users on the server."
                            + "\nvoice   - All users in your connected voice channel.");
                    return;
                }

                List<IUser> raffleUsers = new ArrayList<IUser>();
                try {
                    raffleUsers = getUsers(PoolType.valueOf(args[1].toUpperCase()), guild, message);
                } catch (IllegalArgumentException ex) {
                    BotUtils.sendErrorMessage(channel, 
                            "Unknown pool type. Type \"!raffle\" for help.");
                    return;
                } catch (NullPointerException ex) {
                    BotUtils.sendErrorMessage(channel, 
                            "You are not currently connected to any voice channel on this server!");
                    return;
                }

                raffleUsers.removeIf(vUser -> vUser.equals(message.getAuthor()));

                //Check if the user is the only one in the list
                if (raffleUsers.isEmpty()) {
                    BotUtils.sendErrorMessage(channel, 
                            "You are the only user in the selected pool!"
                            + "\nYou can't win your own raffle silly.");
                    return;
                }

                //Finally send the message
                BotUtils.sendMessage(channel, "Winner", 
                        raffleUsers.get((int) (Math.random() * raffleUsers.size())).getDisplayName(guild));
                return;

            case "setnames":
                //Make sure the command can only be run by the server owner
                if (!isOwner) {
                    BotUtils.sendErrorMessage(channel, "You are not this server's owner.");
                    return;
                }

                //Check if the command has all arguments to it
                if (args.length < 3) {
                    BotUtils.sendUsageMessage(channel, 
                            "!setnames [all/online/offline/voice] [name(s)]"
                            + "\n\nSets the names of users in the selected pool "
                            + "using the entered name or names at random selection."
                            + "\nNames are seperated by a comma WITH NO SPACING (Ben,Ricky,Fartface)."
                            + "\n\nall     - All server users."
                            + "\nonline  - All online users on the server."
                            + "\noffline - All offline users on the server."
                            + "\nvoice   - All users in your connected voice channel.");
                    return;
                }

                List<IUser> usersPool = new ArrayList<IUser>();
                List<String> names = new ArrayList<String>();

                try {
                    usersPool = getUsers(PoolType.valueOf(args[1].toUpperCase()), guild, message);
                } catch (IllegalArgumentException ex) {
                    BotUtils.sendErrorMessage(channel, 
                            "Unknown pool type. Type \".setnames\" for help.");
                    return;
                } catch (NullPointerException ex) {
                    BotUtils.sendErrorMessage(channel, 
                            "You are not currently connected to any voice channel on this server!");
                    return;
                }

                //Check if the list has multiple names, if not, set it to one
                if (args[2].contains(",")) {
                    names = new ArrayList<>(Arrays.asList(args[2].split(",")));
                } else {
                    names.add(args[2]);
                }

                boolean uniqueNames = names.size() >= usersPool.size();

                if (uniqueNames) {
                    BotUtils.sendMessage(channel, 
                            "```There are enough names to give everyone a unique name!```");
                }

                BotUtils.sendInfoMessage(channel, String.format(
                        "Setting the names of %d users with a pool of %d unique name(s)...",
                        usersPool.size(), names.size()));

                for (IUser userToName : usersPool) {
                    int index = (int) (Math.random() * names.size());
                    String name = userToName.getNicknameForGuild(guild);
                    if (name == null) name = userToName.getName();
                    BotUtils.sendMessage(channel, 
                            name + " is now " + names.get(index) + "!");
                    guild.setUserNickname(userToName, names.get(index));
                    if (uniqueNames) {
                        names.remove(index);
                    }
                    try {
                        TimeUnit.MILLISECONDS.sleep(2000);
                    } catch (InterruptedException ex) {
                    }
                }
                BotUtils.sendMessage(channel, "Success", 
                        "Randomly changed all users' names in the specified pool.");
                return;

            case "resetnames":
                //Make sure the command can only be run by the server owner
                if (!isOwner) {
                    BotUtils.sendErrorMessage(channel, "You are not this server's owner.");
                    return;
                }

                if (!hasArgs) {
                    BotUtils.sendUsageMessage(channel, "!resetnames [all/online/offline/voice]"
                            + "\n\nResets the names of users of the selected pool type."
                            + "\n\nall     - All server users."
                            + "\nonline  - All online users on the server."
                            + "\noffline - All offline users on the server."
                            + "\nvoice   - All users in your connected voice channel.");
                    return;
                }

                List<IUser> resetUsers = new ArrayList<IUser>();
                try {
                    resetUsers = getUsers(PoolType.valueOf(args[1].toUpperCase()), guild, message);
                } catch (IllegalArgumentException ex) {
                    BotUtils.sendErrorMessage(channel, 
                            "Unknown pool type. Type \"!resetnames\" for help.");
                    return;
                } catch (NullPointerException ex) {
                    BotUtils.sendErrorMessage(channel, 
                            "You are not currently connected to any voice channel on this server!");
                    return;
                }

                BotUtils.sendInfoMessage(channel, "Resetting all users' names in the specified pool...");

                for (IUser userToReset : resetUsers) {
                    BotUtils.setNickname(guild, userToReset, null);
                }

                return;
            
            //this a terrible fucking command and needs to go or be rewritten
            case "flood":
                //Make sure the command can only be run by the server owner
                if (!isOwner) {
                    BotUtils.sendErrorMessage(channel, "You are not this server's owner.");
                    return;
                }
                
                if (!hasArgs) {
                    BotUtils.sendUsageMessage(channel, "!flood [amount]"
                            + "\n\nRapidly reconnects to your connected voice channel, spamming the users connected."
                            + "\n\namount - The amount of reconnections (300 max).");
                    return;
                }
                
                if (user.getVoiceStateForGuild(guild).getChannel() == null) {
                    BotUtils.sendErrorMessage(channel, 
                            "You are not currently connected to any voice channel on this server!");
                    return;
                }
                
                try {
                    int amount = Integer.parseInt(args[1]);
                    if (amount > 0 && amount <= 300) {
                        for (int i = 1; i <= amount; i++) {
                            try {
                                user.getVoiceStateForGuild(guild).getChannel().join();
                                TimeUnit.MILLISECONDS.sleep(400);
                                user.getVoiceStateForGuild(guild).getChannel().leave();
                                TimeUnit.MILLISECONDS.sleep(400);
                            } catch (InterruptedException ex) {

                            }
                        }
                        BotUtils.sendMessage(channel, "Success", "Flooding complete.");
                        return;
                    }
                } catch (NumberFormatException e) {
                }
                BotUtils.sendErrorMessage(channel, "Amount is not an integer between 1 and 300.");
                return;
                
            case "balance":
                if (hasArgs) {
                    id = UserManager.getUserIDFromName(args[1]);
                    if (id == -1L) {
                        BotUtils.sendErrorMessage(channel, "Specified user was not found in the database.");
                        return;
                    }
                }
                BotUtils.sendMessage(channel, "Balance", "$" 
                        + UserManager.getUserBalance(user.getLongID()));
                return;
                
            case "level":
                if (hasArgs) {
                    id = UserManager.getUserIDFromName(args[1]);
                    if (id == -1L) {
                        BotUtils.sendErrorMessage(channel, "Specified user was not found in the database.");
                        return;
                    }
                }   
                channel.sendMessage(LevelManager.buildInfo(UserManager.getUserFromID(id)));
                return;

            //todo better argument checking
            case "givexp":
                if (!isOwner) {
                    BotUtils.sendErrorMessage(channel, "You are not this server's owner.");
                    return;
                }
                
                if (args.length < 3) {
                    BotUtils.sendUsageMessage(channel, 
                            "!givexp [userID] [amount]"
                            + "\n\nGive xp to the user."
                            + "\n\nuserID - The user's long ID.");
                    return;
                }
                
                LevelManager.addUserXP(guild, Long.parseLong(args[1]), Integer.parseInt(args[2]));
                BotUtils.sendMessage(channel, "Success", "Gave " + args[2] 
                        + "xp to " + UserManager.getUserName(Long.parseLong(args[1])));
                return;     
                
            case "savedata":
                if (!isOwner) {
                    BotUtils.sendErrorMessage(channel, "You are not this server's owner.");
                    return;
                }                
                UserManager.saveDatabase();
                BotUtils.sendInfoMessage(channel, "Database saved.");
                return;
                
            case "info":
                BotUtils.sendInfoMessage(channel,
                        "Zeno Bot " + BotUtils.VERSION + " - Last Updated " 
                                + BotUtils.CHANGEDATE
                                + "\nCoded by Frozen"
                                + "\nMessage me for bot support."
                                + "\nFrozen#9260");
                return;
                
            default:
                BotUtils.sendErrorMessage(channel, "Unknown command. Type \"!help\" for available commands.");
        }
    }
}
