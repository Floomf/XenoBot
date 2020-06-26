package discord.command.game.hangman;

import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.command.game.math.GameMath;
import discord.core.game.AbstractGame;
import discord.core.game.GameManager;
import discord.util.BotUtils;
import discord.util.MessageUtils;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.TextChannel;

import java.util.Random;


public class HangmanCommand extends AbstractCommand {

    public HangmanCommand() {
        super(new String[]{"hangman", "hm"}, 0, CommandCategory.GAME);
    }

    @Override
    public void execute(Message message, TextChannel channel, String[] args) {
        Message gameMessage = channel.createEmbed(embed -> embed.setDescription("Ready..")).block();
        AbstractGame game = new GameHangman(gameMessage, new Member[] {message.getAuthorAsMember().block()});
        GameManager.addGame(gameMessage, game);
        game.start();
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "", "Play a game of Hangman and earn money.");
    }

}
