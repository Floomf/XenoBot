package discord.command.hidden;

import discord.util.BotUtils;
import discord.data.UserManager;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.data.object.user.Progress;
import sx.blah.discord.handle.obj.IMessage;

public class PrestigeCommand extends AbstractCommand {
    
    public PrestigeCommand() {
        super(new String[] {"prestige"}, 0, CommandCategory.HIDDEN);
    }
    
    @Override
    public void execute(IMessage message, String[] args) {
        Progress prog = UserManager.getDBUserFromMessage(message).getProgress();
        if (prog.getPrestige().isMax()) {
            BotUtils.sendInfoMessage(message.getChannel(), "You have already reached the maximum prestige.");
        } else if (prog.isNotMaxLevel()) {
            BotUtils.sendErrorMessage(message.getChannel(), "You must be max level (**"
                    + Progress.MAX_LEVEL + "**) to prestige."
                    + " You can use `!prog` to view your progress.");
        } else { //Is max level
            prog.prestige(message.getGuild());
            BotUtils.sendMessage(message.getChannel(), "Movin' on up", "Promoted to Prestige " 
                    + prog.getPrestige().getNumber() 
                    + (prog.getReincarnation().isReincarnated() ? ", *again?*" : ".")); //handle reincarnated
        }
    }
    
    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "", "Prestige and carry over back to level one."
                + "\n*(Level " + Progress.MAX_LEVEL + ")*");
    }
}
