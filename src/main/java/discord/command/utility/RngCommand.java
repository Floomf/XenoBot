package discord.command.utility;

import discord.core.command.InteractionContext;
import discord.util.BotUtils;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.util.MessageUtils;
import discord4j.core.event.domain.InteractionCreateEvent;
import discord4j.core.object.command.ApplicationCommandInteraction;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.util.ApplicationCommandOptionType;

public class RngCommand extends AbstractCommand {

    public RngCommand() {
        super(new String[]{"rng"}, 1, CommandCategory.UTILITY);
    }

    @Override
    public ApplicationCommandRequest buildSlashCommand() {
        return ApplicationCommandRequest.builder()
                .name("rng")
                .description("Generate a random number")
                .addOption(ApplicationCommandOptionData.builder()
                    .name("max")
                    .description("Max number (generates between 1-max)")
                    .type(ApplicationCommandOptionType.INTEGER.getValue())
                    .required(true)
                    .build())
                .build();
    }

    @Override
    public void execute(InteractionContext context) {
        context.reply(MessageUtils.getEmbed("Result",
                String.valueOf((long) (Math.random() * context.getOptionAsLong("max").get() + 1))));
    }

    @Override
    public void execute(Message message, TextChannel channel, String[] args) {
        try {
            int limit = Integer.parseInt(args[0]);
            if (limit > 0) {
                MessageUtils.sendMessage(channel, "Result",
                        String.valueOf((int) (Math.random() * limit + 1)));
            }
        } catch (NumberFormatException e) {
            MessageUtils.sendErrorMessage(channel, "Please supply an integer greater than zero.");
        }
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "[max]",
                "Generate an number from 1 to the max.");
    }

    @Override
    public boolean isSupportedGlobally() {
        return true;
    }
}
