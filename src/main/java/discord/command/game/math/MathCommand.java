package discord.command.game.math;

import discord.core.command.InteractionContext;
import discord.util.BotUtils;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.manager.GameManager;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.discordjson.json.ApplicationCommandRequest;

public class MathCommand extends AbstractCommand {

    public MathCommand() {
        super("math", 0, CommandCategory.GAME);
    }

    @Override
    public ApplicationCommandRequest buildSlashCommand() {
        return ApplicationCommandRequest.builder()
                .name("math")
                .description("Play a game of Quick Math")
                .build();
    }

    @Override
    public void execute(InteractionContext context) {
        GameManager.createSinglePlayerGame(GameMath.class, "Quick Math", context, 0);
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

