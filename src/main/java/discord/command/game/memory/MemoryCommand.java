package discord.command.game.memory;

import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.manager.GameManager;
import discord.util.BotUtils;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;

public class MemoryCommand extends AbstractCommand {

    public MemoryCommand() {
        super(new String[]{"match", "memory"}, 1, CommandCategory.GAME);
    }

    @Override
    public void execute(Message message, TextChannel channel, String[] args) {
        GameManager.createMultiPlayerGame(GameMemory.class, "Memory Match", channel, message, args);
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "@mention [bet]", "Play a game of Memory Match with someone. " +
                "You can also specify a bet for a money match.");
    }

    @Override
    public boolean isSupportedGlobally() {
        return true;
    }

}
