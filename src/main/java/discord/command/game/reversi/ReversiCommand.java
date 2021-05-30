package discord.command.game.reversi;

import discord.core.command.InteractionContext;
import discord.util.BotUtils;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.manager.GameManager;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.discordjson.json.ApplicationCommandRequest;

public class ReversiCommand extends AbstractCommand {

    public ReversiCommand() {
        super(new String[]{"reversi"}, 1, CommandCategory.GAME);
    }

    @Override
    public ApplicationCommandRequest buildSlashCommand() {
        return GameManager.buildMultiplayerGameSlashCommand(getName(),"Reversi", true);
    }

    @Override
    public ApplicationCommandRequest buildOutsideGuildSlashCommand() {
        return GameManager.buildMultiplayerGameSlashCommand(getName(),"Reversi", false);
    }

    @Override
    public void execute(InteractionContext context) {
        GameManager.createMultiPlayerGame(GameReversi.class, "Reversi", context);
    }

    @Override
    public void execute(Message message, TextChannel channel, String[] args) {
        GameManager.createMultiPlayerGame(GameReversi.class, "Reversi", channel, message, args);
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "@mention [bet]", "Play a game of Reversi (Othello) with someone. " +
                "You can also specify a bet for a money match.");
    }

    @Override
    public boolean isSupportedGlobally() {
        return false;
    }

}

