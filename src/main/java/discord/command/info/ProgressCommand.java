package discord.command.info;

import discord.util.BotUtils;
import discord.data.UserManager;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.data.object.XPChecker;
import discord.data.object.user.Progress;
import discord.util.MessageUtils;
import discord.util.ProfileBuilder;
import discord.data.object.user.DUser;

import java.util.List;
import java.util.function.Consumer;

import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateSpec;
import reactor.core.publisher.Flux;
import discord4j.core.object.entity.Message;

public class ProgressCommand extends AbstractCommand {

    public ProgressCommand() {
        super(new String[]{"progress", "prog", "level", "lvl", "rank", "xp"}, 0, CommandCategory.INFO);
    }

    @Override
    public void execute(Message message, TextChannel channel, String[] args) {
        if (args.length > 0) {
            List<User> mentions = message.getUserMentions().onErrorResume(e -> Flux.empty()).collectList().block();
            if (!mentions.isEmpty()) {
                if (UserManager.databaseContainsUser(mentions.get(0))) {
                    channel.createMessage(spec -> spec.setEmbed(buildProgressInfo(UserManager.getDUserFromID(mentions.get(0).getId().asLong())))).block();
                } else {
                    MessageUtils.sendErrorMessage(channel, "Couldn't find that user in the database. Are they a bot?");
                }
            } else {
                MessageUtils.sendErrorMessage(channel, "Couldn't parse a user. Please @mention them.");
            }
        } else {
            channel.createMessage(spec -> spec.setEmbed(buildProgressInfo(UserManager.getDUserFromMessage(message)))).block();
        }
    }

    private Consumer<EmbedCreateSpec> buildProgressInfo(DUser user) {
        ProfileBuilder builder = new ProfileBuilder(user);
        Progress prog = user.getProg();
        builder.addRank();
        builder.addLevel();
        if (prog.getPrestige().isPrestiged()) {
            builder.addPrestige();
        }
        if (prog.getReincarnation().isReincarnated()) {
            builder.addReincarnation();
        }
        if (prog.isNotMaxLevel()) {
            builder.addXPProgress();
            VoiceState vState = user.asGuildMember().getVoiceState().block();
            //TODO fix nullpointerexception
            if (vState != null && !XPChecker.voiceStateIsNotTalking(vState)) { //Messy but oh well right?
                List<VoiceState> states = vState.getChannel().block().getVoiceStates().collectList().block();
                states.removeIf(state -> state.getUser().block().isBot() || XPChecker.voiceStateIsNotTalking(state));
                if (states.size() >= 2) {
                    builder.addXPRate(states.size());
                }
            }
            builder.addBarProgressToNextLevel();
            if (!prog.getPrestige().isMax()) {
                builder.addBarProgressToMaxLevel();
            }
        }
        return builder.build();
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "@mention", "View you or another user's progress.");
    }

}
