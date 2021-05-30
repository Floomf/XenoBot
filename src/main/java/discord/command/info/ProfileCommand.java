package discord.command.info;

import discord.core.command.InteractionContext;
import discord.data.object.XPChecker;
import discord.util.BotUtils;
import discord.manager.UserManager;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.data.object.user.Prestige;
import discord.util.MessageUtils;
import discord.util.ProfileBuilder;
import discord.data.object.user.Reincarnation;
import discord.data.object.user.DUser;

import java.util.List;
import java.util.function.Consumer;

import discord4j.core.event.domain.InteractionCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.command.ApplicationCommandInteraction;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.object.entity.Message;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.util.ApplicationCommandOptionType;

public class ProfileCommand extends AbstractCommand {

    public ProfileCommand() {
        super(new String[]{"profile"}, 0, CommandCategory.INFO);
    }

    @Override
    public ApplicationCommandRequest buildSlashCommand() {
        return ApplicationCommandRequest.builder()
                .name("profile")
                .description("View your or another user's profile")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("other_user")
                        .description("Specify another user instead")
                        .required(false)
                        .type(ApplicationCommandOptionType.USER.getValue())
                        .build())
                .build();
    }

   @Override
    public void execute(InteractionContext context) {
        DUser dUser = UserManager.getDUserFromUser(context.getOptionAsUser("other_user").orElse(context.getMember()));

        if (dUser == null) {
            context.reply(MessageUtils.getErrorEmbed("Couldn't find that user in the database. Are they a bot?"));
        } else {
            context.reply(buildProfileInfo(dUser));
        }
    }

    @Override
    public void execute(Message message, TextChannel channel, String[] args) {
        if (args.length > 0) {
            User mention = message.getUserMentions().filter(user -> !user.isBot()).blockFirst();
            if (mention != null) {
                if (UserManager.databaseContainsUser(mention)) {
                    channel.createEmbed(buildProfileInfo(UserManager.getDUserFromID(mention.getId().asLong()))).block();
                } else {
                    MessageUtils.sendErrorMessage(channel, "Couldn't find that user in the database. Are they a bot?");
                }
            } else {
                MessageUtils.sendErrorMessage(channel, "Couldn't parse a user. Please @mention them.");
            }
        } else {
            channel.createEmbed(buildProfileInfo(UserManager.getDUserFromMessage(message))).block();
        }
    }

    private Consumer<EmbedCreateSpec> buildProfileInfo(DUser user) {
        ProfileBuilder builder = new ProfileBuilder(user);
        Prestige prestige = user.getProg().getPrestige();
        Reincarnation reincarnation = user.getProg().getReincarnation();
        if (!user.getDesc().isEmpty()) {
            builder.addDesc();
        }

        builder.addRank();
        builder.addLevel();
        if (prestige.isPrestiged()) {
            builder.addPrestige();
        }
        if (reincarnation.isReincarnated()) {
            builder.addReincarnation();
        }
        if (user.getProg().isNotMaxLevel()) {
            builder.addXPProgress();
            VoiceState vState = user.asGuildMember().getVoiceState().blockOptional().orElse(null);
            if (vState != null && vState.getChannel().block() != null && !XPChecker.voiceStateIsNotTalking(vState)) { //Messy but oh well right?
                List<VoiceState> states = vState.getChannel().block().getVoiceStates().collectList().block();
                states.removeIf(state -> state.getUser().block().isBot() || XPChecker.voiceStateIsNotTalking(state));
                if (states.size() >= 2) {
                    builder.addXPRate(states.size());
                }
            }
        }
        if (user.getProg().getXPMultiplier() > 1.0) {
            builder.addXPBoost();
        }
        builder.addTotalXP();
        if (prestige.isPrestiged() || reincarnation.isReincarnated()) {
            builder.addTotalLevel();
        }
        builder.addBalance();
        if (prestige.isPrestiged()) {
            builder.addBadgeCase();
        }
        builder.addTags();
        return builder.build();
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "@mention",
                "View you or another user's profile.");
    }

}
