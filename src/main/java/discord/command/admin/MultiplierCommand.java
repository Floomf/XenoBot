package discord.command.admin;

import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.data.object.Progress;
import discord.util.BotUtils;
import sx.blah.discord.handle.obj.IMessage;

public class MultiplierCommand extends AbstractCommand {
    
    public MultiplierCommand() {
        super(new String[] {"multiplier", "mult"}, 1, CommandCategory.ADMIN);
    }
    
    @Override
    public void execute(IMessage message, String[] args) {
        double multiplier;
        try {
            multiplier = Double.parseDouble(args[0]);
        } catch (NumberFormatException e) {
            BotUtils.sendErrorMessage(message.getChannel(), "Could not parse a valid decimal.");
            return;
        }
        Progress.GLOBAL_XP_MULTIPLIER = multiplier;
        BotUtils.sendInfoMessage(message.getChannel(), "Global XP multiplier updated to `" + multiplier + "x`.");
    }
    
    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "(decimal)", "Change the global XP multiplier for this server."
                + "\n\nCurrent: `" + Progress.GLOBAL_XP_MULTIPLIER + "x`");
    }
}
