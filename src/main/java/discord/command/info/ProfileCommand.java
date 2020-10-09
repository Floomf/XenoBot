package discord.command.info;

import discord.data.object.XPChecker;
import discord.util.BotUtils;
import discord.data.UserManager;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.data.object.user.Prestige;
import discord.util.MessageUtils;
import discord.util.ProfileBuilder;
import discord.data.object.user.Reincarnation;
import discord.data.object.user.DUser;

import java.util.List;
import java.util.function.Consumer;

import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.spec.EmbedCreateSpec;
import reactor.core.publisher.Flux;
import discord4j.core.object.entity.Message;

public class ProfileCommand extends AbstractCommand {

    public ProfileCommand() {
        super(new String[]{"profile", "prof", "bal", "balance", "progress", "prog", "level", "lvl", "rank", "xp"}, 0, CommandCategory.INFO);
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
            VoiceState vState = user.asGuildMember().getVoiceState().block();
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
        return builder.build();
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "@mention",
                "View you or another user's profile.");
    }

}
