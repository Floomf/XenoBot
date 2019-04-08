package discord.command.hidden;

import discord.util.BotUtils;
import discord.core.command.CommandHandler;
import discord.data.UserManager;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.data.object.User;
import java.awt.Color;
import sx.blah.discord.handle.obj.IMessage;

public class ReincarnateCommand extends AbstractCommand {
    
    public ReincarnateCommand() {
        super(new String[] {"reincarnate"}, 0, CommandCategory.HIDDEN);
    }
    
    @Override
    public void execute(IMessage message, String[] args) {
        User user = UserManager.getDBUserFromMessage(message);
        if (!user.getProgress().getPrestige().isMax()) {
            BotUtils.sendMessage(message.getChannel(), "Not yet..", "*I still have this life to live.*");
            return;
        }
        
        if (args.length == 0) {
            BotUtils.sendInfoMessage(message.getChannel(), "Are you **absolutely sure** you want to reincarnate?"
                    + " Your progress this life will be completely reset, losing your level, prestige, badges, and all unlocks."
                    + "\n\nIf you are truly ready, confirm your new life by typing `!reincarnate " 
                    + user.getName().getNick() + '`');
            return;
        }
        
        String name = CommandHandler.combineArgs(0, args);
        if (!name.equals(user.getName().getNick())) {
            BotUtils.sendErrorMessage(message.getChannel(), "Invalid confirmation. If you are truly ready, "
                    + "confirm your new life by typing `!reincarnate " + user.getName().getNick() + "`");
            return;
        }
        user.getProgress().reincarnate(message.getGuild());
        BotUtils.sendMessage(message.getChannel(), "A New Beginning", "You have been reborn into **" 
            + user.getProgress().getReincarnation().getEnglish()+ "**.", Color.CYAN);       
    }
    
    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "", "Reincarnate into a new life and reset **completely** back to level one, "
                + "losing all unlocks, badges, and your prestige, but gain a permanent XP boost.");
    }
             
    
}
