package discord.command.utility;

import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.data.UserManager;
import discord.data.object.Pref;
import discord.data.object.User;
import discord.util.BotUtils;
import java.util.HashMap;
import sx.blah.discord.handle.obj.IMessage;

public class PrefCommand extends AbstractCommand {
    
    public PrefCommand() {
        super(new String[] {"pref", "preference", "setting"}, 1, CommandCategory.UTILITY);
    }
    
    @Override
    public void execute(IMessage message, String[] args) {
        String name = args[0];
        User user = UserManager.getDBUserFromMessage(message);
        HashMap<Pref, Boolean> userPrefs = user.getPrefs();
        
        if (name.equalsIgnoreCase("list")) {
            String prefList = "";
            for (Pref pref : userPrefs.keySet()) {
                prefList += String.format("**%s** - `%s`\n", pref.toString(), userPrefs.get(pref));
            }
            BotUtils.sendMessage(message.getChannel(), "Your Preferences", prefList);
            return;
        }
        
        if (args.length < 2) {
            BotUtils.sendErrorMessage(message.getChannel(), 
                    "Please specify a preference name with a boolean value following it.");
            return;
        }
        
        if (!Pref.contains(args[0])) {
            BotUtils.sendErrorMessage(message.getChannel(), "Unknown preference name. "
                    + "To view a list of available preferences, use `!pref list`."); //Doesnt enforce different prefixes
            return;
        }
        
        if (!(args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("false"))) {
            BotUtils.sendErrorMessage(message.getChannel(), 
                    "Please specify enabling/disabling a preference with `true` or `false`.");
            return;
        }
        
        Pref pref = Pref.valueOf(args[0].toUpperCase());
        Boolean bool = Boolean.valueOf(args[1].toLowerCase());
        
        userPrefs.put(pref, bool);
        
        BotUtils.sendInfoMessage(message.getChannel(), "`" + pref.toString() 
                + "` has been set to `" + bool.toString() + "`.");        
    }
    
    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "[name] [true/false]", "Change a preference for yourself on this guild."
                + "\n\n**Special Arguments**"
                + "\n`!" + alias + " list` - View a list of your preferences.");
    }
    
}
