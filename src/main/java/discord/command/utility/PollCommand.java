package discord.command.utility;

import discord.core.command.InteractionContext;
import discord.data.object.Poll;
import discord.manager.ColorManager;
import discord.manager.PollManager;
import discord.manager.ShopManager;
import discord.util.BotUtils;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.core.object.command.ApplicationCommandOption;

import java.util.ArrayList;
import java.util.List;

public class PollCommand extends AbstractCommand {

    private static final Snowflake POLLS_CHANNEL = Snowflake.of(766191885785366538L);
    private static final Snowflake BANNED_ROLE = Snowflake.of(850255438560624671L);

    public PollCommand() {
        super("poll", 0, CommandCategory.UTILITY);
    }

    @Override
    public ApplicationCommandRequest buildSlashCommand() {
        return ApplicationCommandRequest.builder()
                .name(getName())
                .description("Create a custom poll")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("allow_results")
                        .description("If true, voters can view results after they vote. If false, results are hidden until the poll ends")
                        .required(true)
                        .type(ApplicationCommandOption.Type.BOOLEAN.getValue())
                        .build())
                .addOption(ApplicationCommandOptionData.builder()
                        .name("color")
                        .description("Your poll's color (Type /color list for colors)")
                        .required(true)
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .build())
                .addOption(ApplicationCommandOptionData.builder()
                        .name("title")
                        .description("Your poll's title/question to ask")
                        .required(true)
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .build())
                .addAllOptions(getOptions())
                .build();
    }

    private static List<ApplicationCommandOptionData> getOptions() {
        List<ApplicationCommandOptionData> options = new ArrayList<>();
        for (int i = 1; i <= 2; i++) {
            options.add(ApplicationCommandOptionData.builder()
                .name("option" + i)
                .description("Poll option " + i)
                    .required(true)
                    .type(ApplicationCommandOption.Type.STRING.getValue())
                    .build());
        }
        for (int i = 3; i <= 15; i++) {
            options.add(ApplicationCommandOptionData.builder()
                    .name("option" + i)
                    .description("Poll option " + i)
                    .required(false)
                    .type(ApplicationCommandOption.Type.STRING.getValue())
                    .build());
        }
        return options;
    }

    @Override
    public void execute(InteractionContext context) {
        if (context.getMember().getRoleIds().contains(BANNED_ROLE)) {
            context.replyWithError("You are banned from creating polls.");
            return;
        }

        if (PollManager.hasPoll(context.getMember())) {
            context.replyWithError("You may only have one poll active at a time!");
            return;
        }

        String color = context.getOptionAsString("color");

        if (!(ColorManager.isUnlockedColor(color) || ShopManager.isPurchasedColor(color))) {
            context.replyWithError("That color option doesn't exist! You can view your available colors by typing `/color list`.");
            return;
        }

        if (ColorManager.isUnlockedColor(color) && !context.getDUser().hasUnlocked(ColorManager.getColor(color))) {
            context.replyWithError( "You haven't unlocked that color yet. "
                    + "You can view your available colors by typing `/color list`.");
            return;
        } else if (ShopManager.isPurchasedColor(color) && !context.getDUser().hasPurchased(color)) {
            context.replyWithError("You haven't purchased that color yet. "
                    + "You can view what colors you can buy typing `/shop view`.");
            return;
        }

        String[] optionsStrings = context.getOptions().stream().filter(option -> option.getName().startsWith("option"))
                .map(option -> option.getValue().get().asString()).toArray(String[]::new);

        context.acknowledge();
        Poll poll = new Poll(context.getMember(), context.getGuild().getChannelById(POLLS_CHANNEL).cast(TextChannel.class).block(),
               BotUtils.getGuildRole(color, context.getGuild()).getColor(), 120,
                context.getOptionAsString("title"), optionsStrings,
                context.getOptionAsBoolean("allow_results").get());

        PollManager.addPoll(context.getMember(), poll);

        poll.start();

        context.createFollowupMessageEphemeral("Poll created in <#" + POLLS_CHANNEL.asLong() + ">.");
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "[hex color] [hours] [title] [options]",
                "Create a custom poll.\n\nTo include multiple words in an argument, "
                        + "you must wrap it in quotations."
                        + "\n*Example:* `!poll FF0000 24 \"Candy corn?\" \"Yes, of course!\" No`");
    }

}
