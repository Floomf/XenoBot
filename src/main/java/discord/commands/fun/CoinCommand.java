package discord.commands.fun;

import discord.BotUtils;
import discord.commands.AbstractCommand;
import sx.blah.discord.handle.obj.IMessage;

public class CoinCommand extends AbstractCommand {
    
    public CoinCommand() {
        super(new String[] {"flip", "toss", "coin"}, 0, false);
    }
    
    public void execute(IMessage message, String[] args) {
        String result = "Heads";
        if (Math.random() < 0.5) {
            result = "Tails";
        }
        BotUtils.sendMessage(message.getChannel(), "Result", result);
    }
    
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "", "Flip a coin.");
    }
}
