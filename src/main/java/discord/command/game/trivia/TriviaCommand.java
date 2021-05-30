package discord.command.game.trivia;

import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.core.command.InteractionContext;
import discord.manager.GameManager;
import discord.util.BotUtils;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.discordjson.json.ApplicationCommandRequest;

public class TriviaCommand extends AbstractCommand {

    public TriviaCommand() {
        super(new String[]{"trivia"}, 1, CommandCategory.GAME);
    }

    @Override
    public ApplicationCommandRequest buildSlashCommand() {
        return GameManager.buildMultiplayerGameSlashCommand(getName(),"Trivia", true);
    }

    @Override
    public ApplicationCommandRequest buildOutsideGuildSlashCommand() {
        return GameManager.buildMultiplayerGameSlashCommand(getName(),"Trivia", false);
    }

    @Override
    public void execute(InteractionContext context) {
        GameManager.createMultiPlayerGame(GameTrivia.class, "Trivia 🧠", context);
    }

    @Override
    public void execute(Message message, TextChannel channel, String[] args) {
        GameManager.createMultiPlayerGame(GameTrivia.class, "Trivia 🧠", channel, message, args);
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "@mention [bet]", "Play a game of Trivia with someone. " +
                "You can also specify a bet for a money match.");
    }

    @Override
    public boolean isSupportedGlobally() {
        return false;
    }

}
