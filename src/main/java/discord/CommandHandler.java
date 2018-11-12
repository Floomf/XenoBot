package discord;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.api.events.EventSubscriber;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;

public class CommandHandler {
    /* to be reimplemented in raffle command
    public enum PoolType {
        ALL, ONLINE, OFFLINE, VOICE
    }

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
    }*/

    @EventSubscriber
    public void onMessageEvent(MessageReceivedEvent event) {
        IMessage message = event.getMessage();
        IGuild guild = message.getGuild();
        IChannel channel = message.getChannel();     

        //seperate the contents of the message into an array of strings
        String[] contents = message.getContent().trim().replaceAll("\\s\\s+", " ").split("\\s");        

        if (contents.length == 0) {
            return;
        }

        //make sure the message has the command prefix
        if (!contents[0].startsWith(CommandManager.CMD_PREFIX)) {
            return;
        }

        //make sure the guild has a commands channel
        if (guild.getChannelsByName("commands").isEmpty()) {
            BotUtils.sendInfoMessage(channel,
                    "Please create a new text channel named `#commands`!"
                    + " I will only function properly there. Beep boop.");
            return;
        }
        
        //make sure command is in the commands channel
        if (!channel.getName().equals("commands")) {
            BotUtils.sendInfoMessage(channel,
                    "I will only respond to commands within the `#commands` channel."
                    + " Please type your command again there.");
            return;
        }

        //get command from name
        String name = contents[0].substring(1).toLowerCase();
        AbstractCommand command = CommandManager.getCommand(name);
        
        //make sure command exists
        if (command == null) {
            BotUtils.sendErrorMessage(channel, "Unknown command. Type `!help` for available commands.");
            return;
        }
        
        //check if command requires owner (and if owner is executing it)
        if (command.getCategory().equals(CommandCategory.ADMIN) && !message.getAuthor().equals(guild.getOwner())) {
            BotUtils.sendErrorMessage(channel, "You must be this guild's owner to use this command.");
            return;
        }
        
        //check if command needs args (and if those args exist)
        int argsNeeded = command.getArgsNeeded();
        if (argsNeeded > 0 && !(contents.length > argsNeeded)) {
            BotUtils.sendUsageMessage(channel, command.getUsage(name));
            return;
        }
        
        //create new array with only args (no args = empty array);
        //args array can have more args than needed, perhaps fix it sometime
        String[] args = new String[contents.length - 1];
        for (int i = 0; i < contents.length - 1; i++) {
            args[i] = contents[i + 1];
        }
        command.execute(message, args);
    }        
    
    public static String combineArgs(int index, String[] args) {       
        for (int i = index + 1; i < args.length; i++) {
             args[index] += " " + args[i];
        }
        return args[index];
    }
}
        /*    
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

            /*DOESNT WORK ANYMORE WITH NAMING SYSTEM
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
                  
            case "savedata":
                if (!isOwner) {
                    BotUtils.sendErrorMessage(channel, "You are not this server's owner.");
                    return;
                }                
                UserManager.saveDatabase();
                BotUtils.sendInfoMessage(channel, "Database saved.");
                return;          
                               
        }
        */
