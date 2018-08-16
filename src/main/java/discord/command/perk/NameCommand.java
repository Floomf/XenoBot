package discord.command.perk;

import discord.BotUtils;
import discord.CommandHandler;
import discord.NameManager;
import discord.UserManager;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.object.User;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;

public class NameCommand extends AbstractCommand {
    
    public NameCommand() {
        super(new String[] {"name", "nick"}, 1, CommandCategory.PERK);
    }
    
    public void execute(IMessage message, String[] args) {
        User user = UserManager.getUserFromID(message.getAuthor().getLongID());
        IChannel channel = message.getChannel();
        if (!(user.getLevel() >= 60 || user.getPrestige() > 0)) {
            BotUtils.sendErrorMessage(channel, "You must be at least level **60** to change your name!"
                    + " You can view your progress with `!lvl`.");
            return;
        }
        String name = CommandHandler.combineArgs(0, args);
        if (name.length() > 16) { //name can't be too long
            name = name.substring(0, 15);
        }
        
        if (!name.matches("[ -~]+")) { //regex for char codes between 32-126
            BotUtils.sendErrorMessage(channel, "Your name can only contain letters, "
                    + "numbers, and default keyboard symbols.");
            return;
        }

        if (!UserManager.databaseContainsName(name)) {
            NameManager.setNameOfUser(message.getGuild(), user, name);
            BotUtils.sendInfoMessage(channel, "Your name is now " + name + "!");
            UserManager.saveDatabase();
        } else {
            BotUtils.sendErrorMessage(channel, "Sorry, but that name is already taken.");    
        }
    }
    
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "[new name]", "Change your nickname on this guild."
                + "\n*(Level 60+ or Prestiged)*");
    }
    
}
