package discord.command.utility;

import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.core.command.InteractionContext;
import discord.util.BotUtils;
import discord.util.DiscordColor;
import discord.util.MessageUtils;
import discord4j.common.util.Snowflake;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.discordjson.json.ApplicationCommandRequest;

public class FocusCommand extends AbstractCommand {

    public static final Snowflake FOCUS_ROLE_ID = Snowflake.of(812996717094895666L);
    public static final Snowflake UNFOCUS_MESSAGE_ID = Snowflake.of(894497369981349928L);

    public FocusCommand() {
        super("focus", 0, CommandCategory.UTILITY);
    }

    @Override
    public ApplicationCommandRequest buildSlashCommand() {
        return ApplicationCommandRequest.builder()
                .name(getName())
                .description("Enable focus mode for you (hiding all activity/pings on this server)")
                .build();
    }

    @Override
    public void execute(InteractionContext context) {
        context.replyEphemeral("Focus mode enabled.");
        context.getMember().addRole(FOCUS_ROLE_ID).block();
    }

    @Override
    public void execute(Message message, TextChannel channel, String[] args) {

    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "", "Enable focus mode for you (hiding all activity on this server)");
    }

}
