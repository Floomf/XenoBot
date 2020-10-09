package discord.command.game.twentythree;

import discord.util.BotUtils;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.core.game.GameManager;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;

public class TwentyThreeCommand extends AbstractCommand {

    public TwentyThreeCommand() {
        super(new String[]{"23", "twentythree"}, 1, CommandCategory.GAME);
    }

    @Override
    public void execute(Message message, TextChannel channel, String[] args) {
        GameManager.createMultiPlayerGame(GameTwentyThree.class, "23", channel, message, args);
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "@mention [bet]", "Play a game of 23 with someone. " +
                "You can also specify a bet for a money match.");
    }

    @Override
    public boolean isSupportedGlobally() {
        return true;
    }

}

