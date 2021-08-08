package discord.command.game.checkers;

import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.core.command.InteractionContext;
import discord.manager.GameManager;
import discord.util.BotUtils;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.discordjson.json.ApplicationCommandRequest;

public class CheckersCommand extends AbstractCommand {

    public CheckersCommand() {
        super("checkers", 1, CommandCategory.GAME);
    }

    @Override
    public ApplicationCommandRequest buildSlashCommand() {
        return GameManager.buildMultiplayerGameSlashCommand(getName(), "Checkers", true);
    }

    @Override
    public ApplicationCommandRequest buildOutsideGuildSlashCommand() {
        return GameManager.buildMultiplayerGameSlashCommand(getName(),"Checkers", false);
    }

    @Override
    public void execute(InteractionContext context) {
        GameManager.createMultiPlayerGame(GameCheckers.class, "Checkers", context);
    }

    @Override
    public void execute(Message message, TextChannel channel, String[] args) {
        GameManager.createMultiPlayerGame(GameCheckers.class, "Checkers", channel, message, args);
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "@mention [bet]", "Play a game of Checkers (Draughts) with someone. " +
                "You can also specify a bet for a money match.");
    }

    @Override
    public boolean isSupportedGlobally() {
        return false;
    }

}
