package discord.command.game.twentythree;

import discord.util.BotUtils;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.core.game.GameManager;
import sx.blah.discord.handle.obj.IMessage;

public class TwentyThreeCommand extends AbstractCommand {
    
    public TwentyThreeCommand() {
        super(new String[] {"23", "twentythree"}, 1, CommandCategory.GAME);
    }

    @Override
    public void execute(IMessage message, String[] args) {
        GameManager.processGameCommand(message, "23", GameTwentyThree.class);       
    }
    
    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "@mention", "Play a game of 23 with someone.");
    }
    
}
