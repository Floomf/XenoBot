package discord.command.game.connectfour;

import discord4j.core.object.entity.TextChannel;
import discord.util.BotUtils;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.core.game.GameManager;
import discord4j.core.object.entity.Message;

public class ConnectFourCommand extends AbstractCommand {

    public ConnectFourCommand() {
        super(new String[]{"connect4", "c4"}, 1, CommandCategory.GAME);
    }

    @Override
    public void execute(Message message, TextChannel channel, String[] args) {
        GameManager.processGameCommand(message, channel, args, "Connect 4", GameConnectFour.class);
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "@mention [bet]", "Play a game of Connect 4 with someone. " +
                "You can also specify a bet for a money match.");
    }

}

