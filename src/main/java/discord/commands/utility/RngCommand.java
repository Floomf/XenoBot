package discord.commands.utility;

import discord.BotUtils;
import discord.commands.AbstractCommand;
import discord.commands.CommandCategory;
import sx.blah.discord.handle.obj.IMessage;

public class RngCommand extends AbstractCommand {
    
    public RngCommand() {
        super(new String[] {"rng", "number", "num"}, 1, CommandCategory.UTILITY);
    }
    
    public void execute(IMessage message, String[] args) {
        try {
            int limit = Integer.parseInt(args[0]);
            if (limit > 0) {
                BotUtils.sendMessage(message.getChannel(), "Result",
                        String.valueOf((int) (Math.random() * limit + 1)));
            }
        } catch (NumberFormatException ex) {
            BotUtils.sendErrorMessage(message.getChannel(), 
                "Parameter is not an integer greater than zero.");
        }
    }
    
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "[max]", 
                "Generate an integer from 1 to the max.\n*Max must be greater than 0.*");
    }
}
