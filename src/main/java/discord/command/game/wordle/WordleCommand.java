package discord.command.game.wordle;

import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.command.game.hangman.GameHangman;
import discord.core.command.InteractionContext;
import discord.manager.GameManager;
import discord.util.BotUtils;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.discordjson.json.ApplicationCommandRequest;

public class WordleCommand extends AbstractCommand {

    public WordleCommand() {
        super("wordle", 0, CommandCategory.GAME);
    }

    @Override
    public ApplicationCommandRequest buildSlashCommand() {
        return ApplicationCommandRequest.builder()
                .name("wordle")
                .description("Play a game of Wordle")
                .build();
    }

    @Override
    public void execute(InteractionContext context) {
        GameManager.createSinglePlayerGame(GameWordle.class, "Wordle", context, 0);
    }

    @Override
    public void execute(Message message, TextChannel channel, String[] args) {
        GameManager.createSinglePlayerGame(GameWordle.class, "Wordle", channel, message.getAuthorAsMember().block(), 0);
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
