package discord.command.perk;

import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.core.command.InteractionContext;
import discord.data.object.BadgeMessage;
import discord.data.object.user.Progress;
import discord4j.discordjson.json.ApplicationCommandRequest;

public class BadgeCommand extends AbstractCommand {

    //prestige 2 level 1 (when they actually have 2 badges to choose from)
    public static final int LEVEL_REQUIRED = Progress.MAX_LEVEL * 2 + 1;

    public BadgeCommand() {
        super("badge", 0, CommandCategory.PERK);
    }

    @Override
    public ApplicationCommandRequest buildSlashCommand() {
        return ApplicationCommandRequest.builder()
                .name(getName())
                .description("View your unlocked badges and set one next to your name")
                .build();
    }

    @Override
    public void execute(InteractionContext context) {
        new BadgeMessage(context);
    }

    @Override
    public int getLevelRequired() {
        return LEVEL_REQUIRED;
    }

    @Override
    public String getUsage(String alias) {
        return null;
    }
}
