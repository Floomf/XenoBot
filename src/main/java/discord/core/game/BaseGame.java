package discord.core.game;

import discord.manager.GameManager;
import discord.util.BotUtils;
import discord.util.DiscordColor;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.rest.util.Color;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class BaseGame {

    private final String gameTitle;
    private final TextChannel channel;
    private Message gameMessage;
    private int betAmount;
    private final Thread idleTimerThread;

    private boolean active;
    private int turn;

    private List<Snowflake> playerMessages;
    private int msgDeleteInterval;
    private boolean typingGame;

    public BaseGame(String gameTitle, TextChannel channel, int betAmount) {
        this.gameTitle = gameTitle;
        this.channel = channel;
        this.betAmount = betAmount;
        this.idleTimerThread = new Thread(this::startIdleTimerThread);
        this.active = false;
        this.typingGame = false;
    }

    protected boolean useEmbed() {
        return true;
    }

    abstract protected String getFirstDisplay();

    abstract protected void setup();

    //Games are responsible for registering input handlers here
    abstract protected void onStart();

    abstract protected void onEnd();

    abstract protected void onTurn(String input);

    abstract protected void onTimeout();

    //either a message's content, or a reaction's raw emoji
    abstract protected boolean isValidInput(String input);

    abstract protected String getBoard();

    public final void start() {
        System.out.println("game:" + Thread.currentThread().getId() + " - " + Thread.currentThread().getName());
        active = true;
        turn = 1;
        setup();
        if (!useEmbed()) {
            gameMessage = channel.createMessage(getFirstDisplay()).block();
        } else {
            gameMessage = channel.createEmbed(embed -> {
                embed.setAuthor(gameTitle, "", BotUtils.BOT_AVATAR_URL);// gameMessage.getClient().getSelf().block().getAvatarUrl());
                embed.setDescription(getFirstDisplay());
                embed.setColor(Color.DISCORD_WHITE);
            }).block();
        }
        if (gameMessage == null) { //somehow couldn't send message
            end();
            return;
        }
        idleTimerThread.start();
        onStart();
    }

    protected final void tie(String tieMessage) {
        setGameDisplay(tieMessage, DiscordColor.ORANGE);
        end();
    }

    protected void end() {
        active = false;
        if (Thread.currentThread().getId() != idleTimerThread.getId()) { //don't interrupt, just let it end
            idleTimerThread.interrupt();
        }
        //idleTimer.cancel();
        onEnd();
        GameManager.removeGame(this);
    }

    protected final void registerMessageListener() {
        typingGame = true;
        channel.getClient().getEventDispatcher().on(MessageCreateEvent.class)
                .takeUntil(e -> !active)
                .filter(e -> e.getMessage().getChannelId().equals(channel.getId()) && playerIsInGame(e.getMember().get()))
                .subscribe(e -> {
                    onPlayerInput(e.getMessage().getContent().toLowerCase(), e.getMember().get());
                    if (playerMessages == null) {
                        e.getMessage().delete().block();
                    } else {
                        playerMessages.add(e.getMessage().getId());
                        if (playerMessages.size() == msgDeleteInterval || !active) {
                            deletePlayerMessages();
                        }
                    }
                });
    }

    protected final void registerMessageListener(int msgDeleteInterval) {
        this.msgDeleteInterval = msgDeleteInterval;
        this.playerMessages = new ArrayList<>();
        registerMessageListener();
    }

    protected void deletePlayerMessages() {
        channel.bulkDelete(Mono.just(playerMessages).flatMapMany(Flux::fromIterable)).blockFirst();
        playerMessages.clear();
    }

    protected final void registerReactionListener() {
        channel.getClient().on(ReactionAddEvent.class)
                .takeUntil(e -> !active)
                .filter(e -> e.getMessageId().equals(gameMessage.getId()) && !e.getMember().get().isBot())
                .doOnNext(e -> getGameMessage().removeReaction(e.getEmoji(), e.getUserId()).block()) //ideally do after?
                .filter(e -> playerIsInGame(e.getMember().get()))
                .doFinally(s -> gameMessage.removeAllReactions().block())
                .subscribe(e -> onPlayerReaction(e.getEmoji(), e.getMember().get()));
    }

    protected final void addEmojiReaction(String emoji) {
        gameMessage.addReaction(ReactionEmoji.unicode(emoji)).block();
    }

    public abstract boolean playerIsInGame(Member player);

    abstract protected void onPlayerInput(String input, Member player);

    abstract protected void onPlayerReaction(ReactionEmoji emoji, Member player);

    protected void setBetAmount(int betAmount) {
        this.betAmount = betAmount;
    }

    private void setEntireMessage(String outside, String embedText, Color color) {
        gameMessage.edit(spec -> {
            if (!useEmbed()) {
                spec.setContent(outside + embedText);
                spec.setEmbed(null);
            } else {
                spec.setContent(outside);
                spec.setEmbed(embed -> {
                    embed.setDescription(embedText);
                    embed.setAuthor(gameTitle, "", BotUtils.BOT_AVATAR_URL);// gameMessage.getClient().getSelf().block().getAvatarUrl());
                    embed.setColor(color);
                });
            }
        }).block();
    }

    private void setEntireMessage(String outside, String embedText) {
        setEntireMessage(outside, embedText, Color.DISCORD_WHITE);
    }

    protected final void setGameDisplay(String text) {
        setEntireMessage("", text);
    }

    protected final void setGameDisplay(String text, Color color) {
        setEntireMessage("", text, color);
    }

    protected final void setInfoDisplay(Member memberToPing, String info) {
        setEntireMessage(memberToPing.getMention(), info + "\n\n" + getBoard());
    }

    protected final void setInfoDisplay(String info) {
        setGameDisplay(info + "\n\n" + getBoard());
    }

    public final boolean isActive() {
        return active;
    }

    public final boolean isTypingGame() {
        return typingGame;
    }

    protected final Message getGameMessage() {
        return gameMessage;
    }

    protected final TextChannel getChannel() {
        return channel;
    }

    protected final int getTurn() {
        return turn;
    }

    protected final int getBetAmount() {
        return betAmount;
    }

    protected void setupNextTurn() {
        turn++;
        idleTimerThread.interrupt();
    }

    //Now we are no longer destroying a thread and creating one over and over
    private void startIdleTimerThread() {
        try {
            Thread.sleep(TimeUnit.MINUTES.toMillis(5));
            onTimeout();
        } catch (InterruptedException e) {
            if (!active) {
                Thread.currentThread().interrupt();
            } else {
                startIdleTimerThread();
            }
        }
    }

}
