package discord.command.game.akinator;

import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.core.command.InteractionContext;
import discord.manager.GameManager;
import discord.util.BotUtils;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.discordjson.json.ApplicationCommandRequest;

public class AkinatorCommand extends AbstractCommand {

    public AkinatorCommand() {
        super("akinator", 0, CommandCategory.GAME);
    }

    @Override
    public ApplicationCommandRequest buildSlashCommand() {
        return ApplicationCommandRequest.builder()
                .name(getName())
                .description("Play Akinator")
                .build();
    }

    @Override
    public void execute(InteractionContext context) {
        GameManager.createSinglePlayerGame(GameAkinator.class, "Akinator \uD83E\uDDDE\u200D♂", context, 0);
    }

    @Override
    public void execute(Message message, TextChannel channel, String[] args) {
        GameManager.createSinglePlayerGame(GameAkinator.class, "Akinator \uD83E\uDDDE\u200D♂️", channel, message.getAuthorAsMember().block(), 0);
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
