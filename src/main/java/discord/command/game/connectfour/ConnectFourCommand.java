package discord.command.game.connectfour;

import discord.core.command.InteractionContext;
import discord.util.BotUtils;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.manager.GameManager;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.discordjson.json.ApplicationCommandRequest;

public class ConnectFourCommand extends AbstractCommand {

    public ConnectFourCommand() {
        super(new String[]{"connect4", "c4"}, 1, CommandCategory.GAME);
    }

    @Override
    public ApplicationCommandRequest buildSlashCommand() {
        return GameManager.buildMultiplayerGameSlashCommand(getName(),"Connect 4", true);
    }

    @Override
    public ApplicationCommandRequest buildOutsideGuildSlashCommand() {
        return GameManager.buildMultiplayerGameSlashCommand(getName(),"Connect 4", false);
    }

    @Override
    public void execute(InteractionContext context) {
        GameManager.createMultiPlayerGame(GameConnectFour.class, "Connect 4", context);
    }

    @Override
    public void execute(Message message, TextChannel channel, String[] args) {
        GameManager.createMultiPlayerGame(GameConnectFour.class, "Connect 4", channel, message, args);
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "@mention [bet]", "Play a game of Connect 4 with someone. " +
                "You can also specify a bet for a money match.");
    }

    @Override
    public boolean isSupportedGlobally() {
        return false;
    }

}

