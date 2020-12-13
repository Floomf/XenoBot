package discord.command.game.math;

import discord.util.BotUtils;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.manager.GameManager;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;

public class MathCommand extends AbstractCommand {

    public MathCommand() {
        super(new String[]{"math"}, 0, CommandCategory.GAME);
    }

    @Override
    public void execute(Message message, TextChannel channel, String[] args) {
        GameManager.createSinglePlayerGame(GameMath.class, "Quick Math", channel, message.getAuthorAsMember().block(), 0);
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "", "Play a game of Quick Math and earn money.");
    }

    @Override
    public boolean isSupportedGlobally() {
        return true;
    }

}

