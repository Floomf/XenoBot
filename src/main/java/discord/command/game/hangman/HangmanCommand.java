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

    @Override //TODO hacky and bad
    public void execute(Message message, TextChannel channel, String[] args) {
        GameHangman.AnswerType type = GameHangman.AnswerType.MEDIUM;
        if (args.length > 0) {
            try {
                type = GameHangman.AnswerType.valueOf(args[0].toUpperCase());
            } catch (IllegalArgumentException ex) {
                MessageUtils.sendUsageMessage(channel, getUsage("hangman"));//eh
                return;
            }
        }
        Message gameMessage = channel.createMessage(spec -> spec.setEmbed(embed -> embed.setDescription("Ready..."))).block();
        AbstractGame game = new GameHangman(gameMessage, new Member[] {message.getAuthorAsMember().block(), message.getAuthorAsMember().block()}, type);
        GameManager.addGame(message, game);
        game.start();
        //GameManager.processGameCommand(message, channel, "Quick Math", GameMath.class);
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "[small/medium/large/huge]", "Play a game of Hangman.");
    }

}
