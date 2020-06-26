package discord.command.game.reversi;

import discord4j.core.object.entity.TextChannel;
import discord.util.BotUtils;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.core.game.GameManager;
import discord4j.core.object.entity.Message;


public class ReversiCommand extends AbstractCommand {

    public ReversiCommand() {
        super(new String[]{"reversi", "othello"}, 1, CommandCategory.GAME);
    }

    @Override
    public void execute(Message message, TextChannel channel, String[] args) {
        GameManager.processGameCommand(message, channel, args, "Reversi", GameReversi.class);
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "@mention [bet]", "Play a game of Reversi (Othello) with someone. " +
                "You can also specify a bet for a money match.");
    }

}

