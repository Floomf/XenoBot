package discord.command.game.slide;

import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.core.command.InteractionContext;
import discord.manager.GameManager;
import discord.util.BotUtils;
import discord4j.discordjson.json.ApplicationCommandRequest;

public class SlideCommand extends AbstractCommand {

    public SlideCommand() {
        super("slide", 0, CommandCategory.GAME);
    }

    @Override
    public ApplicationCommandRequest buildSlashCommand() {
        return ApplicationCommandRequest.builder()
                .name(getName())
                .description("Solve a sliding puzzle.")
                .build();
    }

    @Override
    public void execute(InteractionContext context) {
        GameManager.createSinglePlayerGame(GameSlide.class, "Sliding Puzzle", context, 0);
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "[easy/medium/hard]", "Solve a slide puzzle.");
    }

    @Override
    public boolean isSupportedGlobally() {
        return true;
    }
}
