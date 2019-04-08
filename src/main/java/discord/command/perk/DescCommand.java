package discord.command.perk;

import discord.util.BotUtils;
import discord.core.command.CommandHandler;
import discord.data.UserManager;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.data.object.User;
import sx.blah.discord.handle.obj.IMessage;

public class DescCommand extends AbstractCommand {
    
    private static final int LEVEL_REQUIRED = 20; 
    
    public DescCommand() {
        super(new String[]{"desc", "motto", "title"}, 1, LEVEL_REQUIRED, CommandCategory.PERK);
    }

    @Override
    public void execute(IMessage message, String[] args) {
        User user = UserManager.getDBUserFromDUser(message.getAuthor());       
        String desc = BotUtils.validateString(CommandHandler.combineArgs(0, args));
        if (desc.isEmpty()) {
            BotUtils.sendErrorMessage(message.getChannel(), 
                    "Could not parse a valid description. Only basic characters are allowed.");
            return;
        } else if (desc.length() > 55) {
            desc = desc.substring(0, 55);
        }
        user.setDesc(desc);
        BotUtils.sendInfoMessage(message.getChannel(), 
                "How expressive! Updated your description accordingly.");              
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "[text]", 
                "Change or create your description on your profile.");
    }
    
}
