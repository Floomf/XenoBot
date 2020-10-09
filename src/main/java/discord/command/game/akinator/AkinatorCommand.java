package discord.command.game.akinator;

import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.core.game.AbstractGame;
import discord.core.game.GameManager;
import discord.util.BotUtils;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;

public class AkinatorCommand extends AbstractCommand {

    public AkinatorCommand() {
        super(new String[]{"akinator", "akin"}, 0, CommandCategory.GAME);
    }

    @Override
    public void execute(Message message, TextChannel channel, String[] args) {
        GameManager.createSinglePlayerGame(GameAkinator.class, channel, message.getAuthorAsMember().block(), 0);
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "", "Play a game of Akinator.");
    }

    @Override
    public boolean isSupportedGlobally() {
        return true;
    }

}
