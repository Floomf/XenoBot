package discord.command.game.hangman;

import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.core.command.InteractionContext;
import discord.manager.GameManager;
import discord.util.BotUtils;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.discordjson.json.ApplicationCommandRequest;

public class HangmanCommand extends AbstractCommand {

    public HangmanCommand() {
        super("hangman", 0, CommandCategory.GAME);
    }

    @Override
    public ApplicationCommandRequest buildSlashCommand() {
        return ApplicationCommandRequest.builder()
                .name("hangman")
                .description("Play a game of Hangman")
                .build();
    }

    @Override
    public void execute(InteractionContext context) {
        GameManager.createSinglePlayerGame(GameHangman.class, "Hangman", context, 0);
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
