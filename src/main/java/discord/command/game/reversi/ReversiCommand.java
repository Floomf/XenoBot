package discord.command.game.reversi;

import discord.util.BotUtils;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.core.game.GameManager;
import sx.blah.discord.handle.obj.IMessage;

public class ReversiCommand extends AbstractCommand {
    
    public ReversiCommand() {
        super(new String[] {"reversi", "othello"}, 1, CommandCategory.GAME);
    }
    
    @Override
    public void execute(IMessage message, String[] args) {
        GameManager.processGameCommand(message, "Reversi", GameReversi.class);            
    }
    
    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "@mention", "Play a game of Reversi (Othello) with someone.");
    }
    
}
