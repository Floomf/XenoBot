package discord.command.info;

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

import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateSpec;
import reactor.core.publisher.Flux;
import discord4j.core.object.entity.Message;

public class ProfileCommand extends AbstractCommand {

    public ProfileCommand() {
        super(new String[]{"profile", "prof", "bal", "balance"}, 0, CommandCategory.INFO);
    }

    //code copy and paste
    @Override
    public void execute(Message message, TextChannel channel, String[] args) {
        if (args.length > 0) {
            List<User> mentions = message.getUserMentions().onErrorResume(e -> Flux.empty()).collectList().block();
            if (!mentions.isEmpty()) {
                if (UserManager.databaseContainsUser(mentions.get(0))) {
                    channel.createEmbed(buildProfileInfo(UserManager.getDUserFromID(mentions.get(0).getId().asLong()))).block();
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
        }
        if (reincarnation.isReincarnated()) {
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
                "View you or another user's detailed profile.");
    }

}
