package discord.data.object;

import discord.manager.UserManager;
import discord.util.MessageUtils;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Poll {

    public final static String[] EMOJI_LETTERS = {"🇦", "🇧", "🇨", "🇩", "🇪", "🇫", "🇬", "🇭", "🇮", "🇯",
            "🇰", "🇱", "🇲", "🇳", "🇴", "🇵", "🇶", "🇷", "🇸", "🇹"};
    private final static int REWARD_AMOUNT = 150;
    private final static String VOTER_ROLE_MENTION = "<@&780706513902895106>"; //hardcoded for now?

    private boolean active;

    private final Color color;
    private final Message message;
    private final String title;
    private final HashMap<ReactionEmoji, Option> optionsMap;
    private final Instant endInstant;

    private final String pollMessageText;

    static class Option {

        private final String text;
        private final List<Long> voterIds;

        public Option(String text) {
            this.text = text;
            this.voterIds = new ArrayList<>();
        }

        public void addVote(User voter) {
            voterIds.add(voter.getId().asLong());
        }

        public boolean removePossibleVote(User voter) {
            return voterIds.remove(voter.getId().asLong());
        }

        public int getVoteCount() {
            return voterIds.size();
        }

    }

    public Poll(TextChannel channel, Color color, int hours, String title, String[] options) {
        this.color = color;
        this.title = title;
        this.endInstant = Instant.now().plus(hours, ChronoUnit.HOURS).truncatedTo(ChronoUnit.HOURS);
        this.optionsMap = new LinkedHashMap<>();

        for (int i = 0; i < options.length; i++) {
            optionsMap.put(ReactionEmoji.unicode(EMOJI_LETTERS[i]), new Option(options[i]));
        }

        StringBuilder sb = new StringBuilder();
        int optionsAmount = Math.min(options.length, EMOJI_LETTERS.length);

        for (int i = 0; i < optionsAmount; i++) {
            sb.append(EMOJI_LETTERS[i]).append("  ").append(options[i]).append("\n");
        }

        this.pollMessageText = sb.toString();
        this.message = channel.createMessage(spec -> {
            spec.setContent(VOTER_ROLE_MENTION);
            spec.setEmbed(getPollEmbed());
        }).block();

        for (int i = 0; i < options.length; i++) {
            message.addReaction(ReactionEmoji.unicode(EMOJI_LETTERS[i])).block();
        }

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(() -> {
            onPollEnd();
            scheduler.shutdown();
        }, Instant.now().until(endInstant, ChronoUnit.SECONDS), TimeUnit.SECONDS);

        this.active = true;

        message.getClient().on(ReactionAddEvent.class)
                .takeUntil(e -> !active)
                .filter(e -> e.getMessageId().equals(message.getId()) && UserManager.databaseContainsUser(e.getMember().get()))
                .doOnNext(e -> e.getMessage().block().removeReaction(e.getEmoji(), e.getUserId()).block())
                .filter(e -> optionsMap.containsKey(e.getEmoji()) && UserManager.getDUserFromUser(e.getMember().get()).getProg().getTotalLevel() >= 10)
                .subscribe(e -> onVote(e.getMember().get(), e.getEmoji()));
    }

    private Consumer<EmbedCreateSpec> getPollEmbed() {
        return MessageUtils.getEmbed(title, pollMessageText, color).andThen(spec -> {
            spec.setFooter(getTotalVotes() + " " + getVoteWord(getTotalVotes()) + " • Open until", "");
            spec.setTimestamp(endInstant);
        });
    }

    private int getTotalVotes() {
        int votes = 0;
        for (Option o : optionsMap.values()) {
            votes += o.getVoteCount();
        }
        return votes;
    }

    private void onVote(Member voter, ReactionEmoji reaction) {
        Option option = optionsMap.get(reaction);

        if (option.voterIds.contains(voter.getId().asLong())) return;

        String replyText = "";
        if (optionsMap.values().stream().anyMatch(op -> op.removePossibleVote(voter))) { //already voted
            replyText = "You changed your vote to **" + option.text + "** for *" + title + "*.";
            LoggerFactory.getLogger(getClass()).info(voter.getTag() + " changed their vote to option \"" + option.text + "\" for poll \"" + title + "\"");
        } else {
            replyText = "You voted **" + option.text + "** for *" + title + "*.\n\n:dollar: **$" + REWARD_AMOUNT + " earned.**";
            UserManager.getDUserFromMember(voter).addBalance(REWARD_AMOUNT);
            LoggerFactory.getLogger(getClass()).info(voter.getTag() + " voted option \"" + option.text + "\" for poll \"" + title + "\"");
        }

        option.addVote(voter);
        message.edit(spec -> {
            spec.setContent(VOTER_ROLE_MENTION);
            spec.setEmbed(getPollEmbed());
        }).block();

        if (voter.getPrivateChannel().block() != null) {
            MessageUtils.sendMessage(voter.getPrivateChannel().block(), "Thanks for your vote!", replyText, color);
        }
    }

    private void onPollEnd() {
        active = false;
        StringBuilder sb = new StringBuilder();
        List<Option> sorted = optionsMap.values().stream().sorted((op1, op2) -> op2.getVoteCount() - op1.getVoteCount()).collect(Collectors.toList());
        int winningVotes = sorted.get(0).getVoteCount();

        for (Option op : sorted) {
            if (op.getVoteCount() == 0) break;
            sb.append(op.getVoteCount() == winningVotes ? "🏆 **" + op.text + "**" : ":x: " + op.text);
            sb.append(" *(").append(op.getVoteCount()).append(" ").append(getVoteWord(op.getVoteCount())).append(")*\n")
                    .append(getBarForPercentage(((double) op.getVoteCount()) / getTotalVotes())).append("\n\n");
        }

        List<Option> zeroVotes = sorted.stream().filter(op -> op.getVoteCount() == 0).collect(Collectors.toList());
        if (!zeroVotes.isEmpty()) {
            sb.append(":x: **No Votes:**\n");
            zeroVotes.forEach(op -> sb.append(op.text).append(", "));
            sb.deleteCharAt(sb.length() - 2); //hacky
        }

        message.edit(spec -> {
            spec.setContent(null);
            spec.setEmbed(getPollEmbed().andThen(embed -> {
                embed.setDescription(sb.toString());
                embed.setFooter(getTotalVotes() + " " + getVoteWord(getTotalVotes()) + " • Ended ", "");
            }));
        }).block();
        message.removeAllReactions().block();
    }

    private static String getVoteWord(int votes) {
        return votes == 1 ? "vote" : "votes";
    }

    //TODO hybridly resize based on longest poll answer
    private static String getBarForPercentage(double percent) {
        String bar = "■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■";
        return bar.substring(0, (int) (percent * bar.length()))
                + (percent > 0 ? " - " : "") + "**" + new DecimalFormat("#.#").format(percent * 100) + "%** ";
    }


}