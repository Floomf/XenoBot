package discord.command.game.twentythree;

import discord4j.core.object.entity.TextChannel;
import discord.util.BotUtils;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.core.game.GameManager;
import discord4j.core.object.entity.Message;

public class TwentyThreeCommand extends AbstractCommand {

    public TwentyThreeCommand() {
        super(new String[]{"23", "twentythree"}, 1, CommandCategory.GAME);
    }

    @Override
    public void execute(Message message, TextChannel channel, String[] args) {
        GameManager.processGameCommand(message, channel, args, "23", GameTwentyThree.class);
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "@mention [bet]", "Play a game of 23 with someone. " +
                "You can also specify a bet for a money match.");
    }

}

