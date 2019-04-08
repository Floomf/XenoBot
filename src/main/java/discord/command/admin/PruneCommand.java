package discord.command.admin;

import discord.util.BotUtils;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.RequestBuffer;

public class PruneCommand extends AbstractCommand {
    
    public PruneCommand() {
        super(new String[] {"prune"}, 1, CommandCategory.ADMIN); 
    }
    
    @Override
    public void execute(IMessage message, String[] args) {
        if (!args[0].matches("\\d+")) { //not digits   
            BotUtils.sendErrorMessage(message.getChannel(), "Could not parse amount of messages to prune.");
            return;
        }
        
        int amount = Integer.parseInt(args[0]);
        if (amount > 50 || amount < 1) {
            BotUtils.sendErrorMessage(message.getChannel(), 
                    "Invalid amount of messages to prune! You may prune up to 50 at a time.");
            return;
        }
        RequestBuffer.request(() -> message.getChannel().getMessageHistory(amount + 1).bulkDelete()); 
        //amount + 1 to include their prune command message       
    }
    
    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "[amount]", 
                "Mass prune (delete) messages in this channel.\nYou may prune up to 50 messages at a time.");
    }
    
}
    
