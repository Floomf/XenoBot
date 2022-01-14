package discord.data.object;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import discord.manager.PollManager;
import discord.manager.UserManager;
import discord.util.BotUtils;
import discord.util.MessageUtils;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.spec.legacy.LegacyEmbedCreateSpec;
import discord4j.rest.util.Color;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Poll {

    public static class SerializableData {

        public final long ownerID;
        public final long channelID;
        public final long messageID;
        public final String title;
        public final Option[] options;
        public final boolean allowResults;

        public SerializableData(@JsonProperty("ownerID") long ownerID,
                                @JsonProperty("channelID") long channelID,
                                @JsonProperty("messageID") long messageID,
                                @JsonProperty("title") String title,
                                @JsonProperty("options") Option[] options,
                                @JsonProperty("allowResults") boolean allowResults) {
            this.ownerID = ownerID;
            this.channelID = channelID;
            this.messageID = messageID;
            this.title = title;
            this.options = options;
            this.allowResults = allowResults;
        }
    }

    public final static String[] EMOJI_LETTERS = {"🇦", "🇧", "🇨", "🇩", "🇪", "🇫", "🇬", "🇭", "🇮", "🇯",
            "🇰", "🇱", "🇲", "🇳", "🇴", "🇵", "🇶", "🇷", "🇸", "🇹"};

    private final User owner;
    private final Color color;
    private final Message message;
    private final String title;
    private final HashMap<ReactionEmoji, Option> optionsMap;
    private final boolean allowResults;
    private final Instant endInstant;

    private final String pollMessageText;

    private boolean active;
    private ScheduledFuture<?> future;

    static class Option {

        public final String text;
        public final List<Long> voterIds;

        public Option(@JsonProperty("text") String text,
                      @JsonProperty("voterIDs") List<Long> voterIds) {
            this.text = text;
            this.voterIds = voterIds;
        }

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

        @JsonIgnore
        public int getVoteCount() {
            return voterIds.size();
        }

    }

    public Poll(User owner, TextChannel channel, Color color, int hours, String title, String[] options, boolean allowResults) {
        this.owner = owner;
        this.color = color;
        this.title = title;
        this.endInstant = Instant.now().plus(hours, ChronoUnit.HOURS).truncatedTo(ChronoUnit.HOURS);
        this.optionsMap = new LinkedHashMap<>();
        this.allowResults = allowResults;

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
            spec.setEmbed(getPollEmbed());
            if (allowResults) {
                spec.setComponents(ActionRow.of(Button.primary("results", "View current results"),
                        Button.secondary("delete", "Delete")));
            } else {
                spec.setComponents(ActionRow.of(Button.secondary("delete", "Delete")));
            }
        }).block();

        for (int i = 0; i < options.length; i++) {
            message.addReaction(ReactionEmoji.unicode(EMOJI_LETTERS[i])).block();
        }
    }

    public Poll(SerializableData data, GatewayDiscordClient client) {
        this.owner = client.getUserById(Snowflake.of(data.ownerID)).block();
        this.message = client.getMessageById(Snowflake.of(data.channelID), Snowflake.of(data.messageID)).block();
        this.title = data.title;
        this.optionsMap = new HashMap<>();
        for (int i = 0; i < data.options.length; i++) {
            optionsMap.put(ReactionEmoji.unicode(EMOJI_LETTERS[i]), data.options[i]);
        }
        this.allowResults = data.allowResults;

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < data.options.length; i++) {
            sb.append(EMOJI_LETTERS[i]).append("  ").append(data.options[i].text).append("\n");
        }

        this.pollMessageText = sb.toString();
        this.color = message.getEmbeds().get(0).getColor().get();
        this.endInstant = message.getEmbeds().get(0).getTimestamp().get();
        message.edit(spec -> spec.addEmbed(getPollEmbed())).block();
    }

    public void start() {
        this.active = true;
        message.getClient().on(ReactionAddEvent.class)
                .takeUntil(e -> !active)
                .filter(e -> e.getMessageId().equals(message.getId()) && UserManager.databaseContainsUser(e.getMember().get()))
                .doOnNext(e -> e.getMessage().block().removeReaction(e.getEmoji(), e.getUserId()).block())
                .subscribe(this::onPollReaction);
        message.getClient().on(ButtonInteractionEvent.class)
                .takeUntil(e -> !active)
                .filter(e -> e.getInteraction().getMessage().map(Message::getId).map(id -> id.equals(message.getId())).orElse(false))
                .subscribe(this::onPollButtonInteract);

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        future = scheduler.schedule(() -> {
            end();
            scheduler.shutdown();
        }, Instant.now().until(endInstant, ChronoUnit.SECONDS), TimeUnit.SECONDS);
    }

    private void sendResults(ButtonInteractionEvent event) {
        event.reply(spec -> {
            spec.setEphemeral(true);
            spec.addEmbed(embed -> {
                embed.setColor(color);
                embed.setDescription(getResults());
                embed.setAuthor("Current Results", "", BotUtils.BOT_AVATAR_URL);
            });
        }).block();
    }

    private void onPollButtonInteract(ButtonInteractionEvent event) {
        String button = event.getCustomId();
        if (button.equals("results")) {
            if (hasVoted(event.getInteraction().getUser())) {
                sendResults(event);
            } else {
                event.reply(spec -> {
                    spec.setContent("You must vote first before viewing the current results!");
                    spec.setEphemeral(true);
                }).block();
            }
        } else if (button.equals("delete")) {
            if (event.getInteraction().getUser().equals(owner)
                    || event.getInteraction().getGuild().block().getOwnerId().equals(event.getInteraction().getUser().getId())) {
                future.cancel(true);
                delete();
            } else {
                event.reply(spec -> {
                    spec.setContent("You can't delete a poll that you didn't create!");
                    spec.setEphemeral(true);
                }).block();
            }
        }
    }

    private void onPollReaction(ReactionAddEvent event) {
        if (optionsMap.containsKey(event.getEmoji())
                && (!allowResults || !hasVoted(event.getMember().get())) //allow changing vote if they cant view results
                && UserManager.getDUserFromUser(event.getMember().get()).getProg().getTotalLevel() >= 5) {
            onVote(event.getMember().get(), event.getEmoji());
        }
    }

    private void onVote(Member voter, ReactionEmoji reaction) {
        Option option = optionsMap.get(reaction);

        if (option.voterIds.contains(voter.getId().asLong())) return;

        String replyText = "";
        if (optionsMap.values().stream().anyMatch(op -> op.removePossibleVote(voter))) { //already voted
            replyText = "You changed your vote to **" + option.text + "** for *" + title + "*.";
            LoggerFactory.getLogger(getClass()).info(voter.getTag() + " changed their vote to option \"" + option.text + "\" for poll \"" + title + "\"");
        } else {
            replyText = "You voted **" + option.text + "** for *" + title + "*.";
            LoggerFactory.getLogger(getClass()).info(voter.getTag() + " voted option \"" + option.text + "\" for poll \"" + title + "\"");
        }

        option.addVote(voter);
        message.edit(spec -> spec.setEmbed(getPollEmbed())).block();

        if (voter.getPrivateChannel().block() != null) {
            MessageUtils.sendMessage(voter.getPrivateChannel().block(), "Thanks for your vote!", replyText, color);
        }
        PollManager.savePolls();
    }

    private void delete() {
        active = false;
        message.delete().block();
        PollManager.removePoll(owner); //bad to put here?
        PollManager.savePolls();
    }

    private void end() {
        active = false;

        message.edit(spec -> {
            spec.addEmbed(MessageUtils.getEmbed(title, getResults(), color).andThen(embed ->
                    embed.setFooter(getTotalVotes() + " " + getVoteWord(getTotalVotes())
                            + " • Created by " + UserManager.getDUserFromUser(owner).getName().getNick(),
                            owner.getAvatarUrl())));
            spec.setComponents();
        }).block();
        message.removeAllReactions().block();

        if (owner.getPrivateChannel().block() != null) {
            MessageUtils.sendMessage(owner.getPrivateChannel().block(), "Alert!",
                    "Your poll titled *" + title + "* has ended. Go check out the results!", color);
        }

        PollManager.removePoll(owner);
        PollManager.savePolls();
    }

    private boolean hasVoted(User user) {
        return optionsMap.values().stream().anyMatch(o -> o.voterIds.stream().anyMatch(id -> id == user.getId().asLong()));
    }

    private Consumer<LegacyEmbedCreateSpec> getPollEmbed() {
        return MessageUtils.getEmbed(title, pollMessageText + "\n(Made by " + owner.getMention() + ")", color).andThen(spec -> {
            spec.setFooter(getTotalVotes() + " " + getVoteWord(getTotalVotes()) + " • Open until", owner.getAvatarUrl());
            spec.setTimestamp(endInstant);
        });
    }

    private String getResults() {
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
        return sb.toString();
    }

    private int getTotalVotes() {
        int votes = 0;
        for (Option o : optionsMap.values()) {
            votes += o.getVoteCount();
        }
        return votes;
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

    public SerializableData getSerializableData() {
        return new SerializableData(owner.getId().asLong(),
                message.getChannelId().asLong(),
                message.getId().asLong(), title,
                optionsMap.values().toArray(new Option[0]),
                allowResults);
    }

}