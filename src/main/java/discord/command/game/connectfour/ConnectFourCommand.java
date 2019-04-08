package discord.command.game.connectfour;

import discord.util.BotUtils;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.core.game.GameManager;
import sx.blah.discord.handle.obj.IMessage;

public class ConnectFourCommand extends AbstractCommand {
    
    public ConnectFourCommand() {
        super(new String[] {"connect4", "c4"}, 1, CommandCategory.GAME);
    }

    @Override
    public void execute(IMessage message, String[] args) {
        GameManager.processGameCommand(message, "Connect 4", GameConnectFour.class);       
    }
    
    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "@mention", "Play a game of Connect 4 with someone.");
    }
    
}
