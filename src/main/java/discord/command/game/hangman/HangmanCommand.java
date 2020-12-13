package discord.command.game.hangman;

import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.manager.GameManager;
import discord.util.BotUtils;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;

public class HangmanCommand extends AbstractCommand {

    public HangmanCommand() {
        super(new String[]{"hangman", "hm"}, 0, CommandCategory.GAME);
    }

    @Override
    public void execute(Message message, TextChannel channel, String[] args) {
        GameManager.createSinglePlayerGame(GameHangman.class, "Hangman", channel, message.getAuthorAsMember().block(), 0);
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "", "Play a game of Hangman and earn money.");
    }

    @Override
    public boolean isSupportedGlobally() {
        return true;
    }

}
