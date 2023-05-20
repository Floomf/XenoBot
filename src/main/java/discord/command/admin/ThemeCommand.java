package discord.command.admin;

import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.core.command.CommandHandler;
import discord.core.command.InteractionContext;
import discord.data.object.user.DUser;
import discord.data.object.user.Rank;
import discord.listener.EventsHandler;
import discord.manager.UserManager;
import discord.util.BotUtils;
import discord.util.MessageUtils;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ModalSubmitInteractionEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.TextInput;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.core.spec.InteractionPresentModalSpec;
import discord4j.core.util.OrderUtil;
import discord4j.discordjson.json.ApplicationCommandRequest;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ThemeCommand extends AbstractCommand {

    private static final Snowflake CHANNEL_1 = Snowflake.of(811090544485990441L);
    private static final Snowflake CHANNEL_2 = Snowflake.of(901780563457880066L);
    private static final Snowflake CHANNEL_3 = Snowflake.of(298179139221848064L);

    private static final Snowflake ROLE_BOTS = Snowflake.of(827489401185239050L);

    public ThemeCommand() {
        super("theme", 2, CommandCategory.ADMIN);
    }

    @Override
    public ApplicationCommandRequest buildSlashCommand() {
        return ApplicationCommandRequest.builder()
                .name(getName())
                .description("Change the theme on The Realm")
                .build();
    }

    @Override
    public void execute(InteractionContext context) {
        if (!(context.getUser().getId().equals(Snowflake.of(194934170005274624L))
                || context.getUser().getId().equals(Snowflake.of(98235887866908672L))
                || context.getUser().getId().equals(Snowflake.of(363601835245699074L)))) {
            context.replyWithError("You don't have permission to use this command.");
            return;
        }

        context.event.presentModal(InteractionPresentModalSpec.create()
                .withCustomId("theme_submit")
                .withTitle("The Realm Theme")
                .withComponents(
                        ActionRow.of(TextInput.paragraph("ranks", "5 Ranks (Highest to lowest, one per line)", 1, 400)
                                .prefilled(getCurrentRanks()).required()),
                        ActionRow.of(TextInput.small("bots_rank", "Rank for Bots", 1, 100)
                                .prefilled(context.getGuild().getRoleById(ROLE_BOTS).block().getName())),
                        ActionRow.of(TextInput.paragraph("channels", "3 Voice Channels (One per line)", 1, 300)
                                .prefilled(getCurrentVoiceChannels(context.getGuild()))),
                        ActionRow.of(TextInput.small("afk_channel", "AFK Voice Channel", 1, 100)
                                .prefilled(context.getGuild().getAfkChannel().block().getName())))).block();
    }

    private static String getCurrentVoiceChannels(Guild guild) {
        String channelsString = "";
        List<VoiceChannel> channels = guild.getChannels().ofType(VoiceChannel.class).collectList().block();
        //TODO don't rely on hardcoded voice channel IDS
        channelsString += channels.stream().filter(c -> c.getId().equals(CHANNEL_1)).findFirst().get().getName() + "\n";
        channelsString += channels.stream().filter(c -> c.getId().equals(CHANNEL_2)).findFirst().get().getName() + "\n";
        channelsString += channels.stream().filter(c -> c.getId().equals(CHANNEL_3)).findFirst().get().getName();
        return channelsString;
    }

    private static String getCurrentRanks() {
        String ranks = "";
        for (int i = Rank.RANKS.length - 1; i > 0; i--) {
            ranks += Rank.RANKS[i].getName() + "\n";
        }
        ranks += Rank.RANKS[0].getName();
        return ranks;
    }

    public static void onSubmitTheme(ModalSubmitInteractionEvent event) {
        List<TextInput> inputs = event.getComponents(TextInput.class);
        if (inputsAreValid(inputs)) {
            event.deferEdit().block();
            LoggerFactory.getLogger(ThemeCommand.class).info("User " + event.getInteraction().getUser().getId().asString() + " submitted a theme modal");

            Guild guild = event.getClient().getGuildById(EventsHandler.THE_REALM_ID).block();
            List<VoiceChannel> channels = OrderUtil.orderGuildChannels(guild.getChannels()).ofType(VoiceChannel.class)
                    .filter(c -> c.getCategoryId().map(vc -> vc.equals(Snowflake.of(827480733270016012L))).orElse(false)).collectList().block();

            String[] newChannelNames = getArrayFromTextInput(inputs.stream().filter(i -> i.getCustomId().equals("channels")).findFirst().get());
            for (int i = 0; i < channels.size(); i++) {
                if (!channels.get(i).getName().equals(newChannelNames[i])) {
                    LoggerFactory.getLogger(ThemeCommand.class).info("Changed voice channel \"" + channels.get(i).getName() + "\" to \"" + newChannelNames[i] + "\"");
                    channels.get(i).edit().withName(newChannelNames[i]).block();
                }
            }

            String newAfkChannelName = inputs.stream().filter(i -> i.getCustomId().equals("afk_channel")).findFirst().get().getValue().orElse("");
            VoiceChannel afkChannel = guild.getAfkChannel().block();
            if (!afkChannel.getName().equals(newAfkChannelName)) {
                LoggerFactory.getLogger(ThemeCommand.class).info("Changed AFK voice channel \"" + afkChannel.getName() + "\" to \"" + newAfkChannelName + "\"");
                afkChannel.edit().withName(newAfkChannelName).block();
            }

            List<Role> rankRoles = OrderUtil.orderRoles(guild.getRoles()).filter(Rank::isRankRole).collectList().block();
            String[] newRanks = getArrayFromTextInput(inputs.stream().filter(i -> i.getCustomId().equals("ranks")).findFirst().get());
            Collections.reverse(Arrays.asList(newRanks));
            for (int i = 0; i < rankRoles.size(); i++) {
                if (!rankRoles.get(i).getName().equals(newRanks[i])) {
                    LoggerFactory.getLogger(ThemeCommand.class).info("Changed rank role \"" + rankRoles.get(i).getName() + "\" to \"" + newRanks[i] + "\"");
                    rankRoles.get(i).edit().withName(newRanks[i]).block();
                    Rank.RANKS[i].setName(newRanks[i]);
                }
            }

            String botRank = trimEnd(inputs.stream().filter(i -> i.getCustomId().equals("bots_rank")).findFirst().get().getValue().orElse(""));
            Role botRole = guild.getRoleById(ROLE_BOTS).block();
            if (!botRole.getName().equals(botRank)) {
                LoggerFactory.getLogger(ThemeCommand.class).info("Changed bot role \"" + botRole.getName() + "\" to \"" + botRank + "\"");
                botRole.edit().withName(botRank).block();
            }

            Rank.saveRanks();
        }
    }

    private static String trimEnd(String string) {
        return string.length() > 100 ? string.substring(0, 100) : string;
    }

    private static boolean inputsAreValid(List<TextInput> inputs) {
        for (TextInput input : inputs) {
            if (input.getCustomId().equals("ranks") || input.getCustomId().equals("channels")) {
                String[] elements = getArrayFromTextInput(input);
                if (input.getCustomId().equals("ranks") && elements.length != Rank.RANKS.length) {
                    return false;
                } else if (input.getCustomId().equals("channels") && elements.length != 3) {
                    return false;
                }
                for (String element : elements) {
                    if (element.isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static String[] getArrayFromTextInput(TextInput input) {
        String[] elements = input.getValue().orElse("").trim().split("\n");
        for (int i = 0; i < elements.length; i++) {
            elements[i] = trimEnd(elements[i]);
        }
        return elements;
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "", "");
    }

}
