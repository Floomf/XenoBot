package discord.command.perk;

import discord.BotUtils;
import discord.CommandHandler;
import discord.UserManager;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.object.User;
import sx.blah.discord.handle.obj.IMessage;

public class DescCommand extends AbstractCommand {
    
    public DescCommand() {
        super(new String[]{"desc", "motto", "title"}, 1, CommandCategory.PERK);
    }

    @Override
    public void execute(IMessage message, String[] args) {
        User user = UserManager.getDBUserFromDUser(message.getAuthor());
        if (!(user.getProgress().getTotalLevel() >= 20)) {
            BotUtils.sendErrorMessage(message.getChannel(),
                    "You must be at least level **20** create your description!"
                    + " You can view your progress with '!prog'.");
            return;
        }
        
        String desc = BotUtils.validateString(CommandHandler.combineArgs(0, args));
        if (desc.isEmpty()) {
            BotUtils.sendErrorMessage(message.getChannel(), 
                    "Could not parse a valid description. Only basic characters are allowed.");
            return;
        } else if (desc.length() > 45) {
            desc = desc.substring(0, 45);
        }
        user.setDesc(desc);
        BotUtils.sendInfoMessage(message.getChannel(), 
                "How expressive! Updated your description accordingly.");              
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "[text]", 
                "Change or create your description on your profile."
                + "\n*(Level 20+ or Prestiged)*");
    }
    
}
