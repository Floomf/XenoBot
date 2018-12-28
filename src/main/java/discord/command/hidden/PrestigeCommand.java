package discord.command.hidden;

import discord.BotUtils;
import discord.UserManager;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.object.Progress;
import sx.blah.discord.handle.obj.IMessage;

public class PrestigeCommand extends AbstractCommand {
    
    public PrestigeCommand() {
        super(new String[] {"prestige"}, 0, CommandCategory.HIDDEN);
    }
    
    public void execute(IMessage message, String[] args) {
        Progress prog = UserManager.getDBUserFromMessage(message).getProgress();
        if (prog.getPrestige().isMax()) {
            BotUtils.sendInfoMessage(message.getChannel(), "You have already reached the maximum prestige.");
        } else if (!(prog.isMaxLevel())) {
            BotUtils.sendErrorMessage(message.getChannel(), "You must be max level (**"
                    + Progress.MAX_LEVEL + "**) to prestige."
                    + " You can use `!prog` to view your progress.");
        } else { //can prestige
            prog.prestige(message.getGuild());
            BotUtils.sendMessage(message.getChannel(), "Movin' on up", "Welcome to Prestige " 
                    + prog.getPrestige().getNumber() 
                    + (prog.getReincarnation().isReincarnated() ? ", *again?*" : ".")); //handle reincarnated
        }
    }
    
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "", "Prestige and carry over back to level one."
                + "\n*(Level " + Progress.MAX_LEVEL + ")*");
    }
}
