package discord.command.admin;

import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.data.object.user.Progress;
import discord.util.BotUtils;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import sx.blah.discord.handle.obj.IMessage;

public class MultiplierCommand extends AbstractCommand {
    
    public MultiplierCommand() {
        super(new String[] {"multiplier", "mult"}, 2, CommandCategory.ADMIN);
    }
    
    @Override
    public void execute(IMessage message, String[] args) {
        double multiplier;
        int hours;
        try {
            multiplier = Double.parseDouble(args[0]);
            hours = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            BotUtils.sendErrorMessage(message.getChannel(), 
                    "Couldn't parse a valid multiplier or amount of hours.");
            return;
        }
        
        if (hours < 1) {
            BotUtils.sendErrorMessage(message.getChannel(), "Amount of hours must be at least 1.");
            return;
        }
        
        Progress.GLOBAL_XP_MULTIPLIER = multiplier;
        BotUtils.sendInfoMessage(message.getGuild().getChannelsByName("general").get(0), 
                "`" + multiplier + "x` XP is now being given out for the next `" + hours + "h`.");
        
        final ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);
        exec.schedule(() -> {
            Progress.GLOBAL_XP_MULTIPLIER = 1.0; //hardcoded
            BotUtils.sendInfoMessage(message.getGuild().getChannelsByName("general").get(0), 
                "Global XP multiplier has returned to `1.0x`");
        }, hours, TimeUnit.HOURS);
    }
    
    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "(multiplier) (hours)", "Change the global XP multiplier for this server."
                + "\n\nCurrent: `" + Progress.GLOBAL_XP_MULTIPLIER + "x`");
    }
}
