package discord.command.hidden;

import discord.BotUtils;
import discord.UserManager;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.object.Progress;
import discord.object.User;
import sx.blah.discord.handle.obj.IMessage;

public class PrestigeCommand extends AbstractCommand {
    
    public PrestigeCommand() {
        super(new String[] {"prestige"}, 0, CommandCategory.HIDDEN);
    }
    
    public void execute(IMessage message, String[] args) {
        User user = UserManager.getDBUserFromMessage(message);
        if (!(user.getProgress().isMaxLevel())) {
                    BotUtils.sendErrorMessage(message.getChannel(), "You must be level **"
                            + Progress.MAX_LEVEL + "** to prestige."
                            + " You can view your progress with `!lvl`.");
        } else {
            user.getProgress().prestige(message.getGuild());
        }
    }
    
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "", "Prestige and carry over back to level one."
                + "\n*(Level 80)*");
    }
}
