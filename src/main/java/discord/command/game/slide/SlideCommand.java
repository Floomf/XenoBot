package discord.command.game.slide;

import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.core.game.AbstractGame;
import discord.core.game.GameManager;
import discord.util.BotUtils;
import discord.util.MessageUtils;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;

public class SlideCommand extends AbstractCommand {

    public SlideCommand() {
        super(new String[]{"slide"}, 1, CommandCategory.GAME);
    }

    @Override
    public void execute(Message message, TextChannel channel, String[] args) {
        GameSlide.Difficulty difficulty = GameSlide.Difficulty.fromString(args[0]);

        if (difficulty == null) {
            MessageUtils.sendErrorMessage(channel, "Please specify a valid difficulty (easy/medium/hard).");
            return;
        }

        Message gameMessage = channel.createMessage("Ready..").block();
        AbstractGame game = new GameSlide(gameMessage, new Member[] {message.getAuthorAsMember().block()}, difficulty);
        GameManager.addGame(gameMessage, game);
        game.start();
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "[easy/medium/hard]", "Solve a slide puzzle.");
    }
}
