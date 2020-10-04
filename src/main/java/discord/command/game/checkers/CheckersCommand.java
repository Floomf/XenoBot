package discord.command.game.checkers;

import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.core.game.GameManager;
import discord.util.BotUtils;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;

public class CheckersCommand extends AbstractCommand {

    public CheckersCommand() {
        super(new String[]{"checkers", "draughts"}, 1, CommandCategory.GAME);
    }

    @Override
    public void execute(Message message, TextChannel channel, String[] args) {
        GameManager.processGameCommand(message, channel, args, "Checkers", GameCheckers.class);
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "@mention [bet]", "Play a game of Checkers (Draughts) with someone. " +
                "You can also specify a bet for a money match.");
    }

}
