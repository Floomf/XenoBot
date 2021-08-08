package discord.core.game;

import discord.core.command.InteractionContext;
import discord.manager.GameManager;
import discord.util.BotUtils;
import discord.util.DiscordColor;
import discord.util.MessageUtils;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ButtonInteractEvent;
import discord4j.core.event.domain.interaction.ComponentInteractEvent;
import discord4j.core.event.domain.interaction.SelectMenuInteractEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.component.LayoutComponent;
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

    //TODO BETTER SOLUTION
    protected ComponentInteractEvent componentEvent = null;

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

    protected LayoutComponent[] getComponents() {
        return new LayoutComponent[0];
    }

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
        active = true;
        turn = 1;
        setup();
        if (!useEmbed()) {
            gameMessage = channel.createMessage(getFirstDisplay()).block();
        } else {
            gameMessage = channel.createMessage(spec -> {
                spec.addEmbed(embed -> {
                    embed.setAuthor(gameTitle, "", BotUtils.BOT_AVATAR_URL);// gameMessage.getClient().getSelf().block().getAvatarUrl());
                    embed.setDescription(getFirstDisplay());
                    embed.setColor(Color.DISCORD_WHITE);
                });
                spec.setComponents(getComponents());
            }).block();
        }
        if (gameMessage == null) { //somehow couldn't send message
            end();
            return;
        }
        idleTimerThread.start();
        onStart();
    }

    //Used for multiplayer games, coming from GameRequest
    public final void startFromMessage(Message message) {
        gameMessage = message;
        active = true;
        turn = 1;
        setup();
        if (!useEmbed()) {
            gameMessage.edit(spec -> {
                spec.setContent(getFirstDisplay());
                spec.setComponents(getComponents());
            }).block();
        } else {
            gameMessage.edit(spec -> {
                spec.addEmbed(MessageUtils.getEmbed(gameTitle, getFirstDisplay(), Color.DISCORD_WHITE));
                spec.setComponents(getComponents());
            }).block();
        }
        idleTimerThread.start();
        onStart();
    }

    //Used for singleplayer games, not from GameRequest
    public final void startFromInteraction(InteractionContext context) {
        active = true;
        turn = 1;
        setup();
        //Only way I know how to store the message from an interaction
        context.getChannel().getClient().on(MessageCreateEvent.class)
                .map(MessageCreateEvent::getMessage)
                .filter(message -> message.getInteraction().map(interaction -> interaction.getId().equals(context.event.getInteraction().getId())).orElse(false))
                .take(1)
                .subscribe(message -> {
                    gameMessage = message;
                    idleTimerThread.start();
                    onStart();
                });
        context.event.reply(spec -> {
            if (!useEmbed()) {
                spec.setContent(getFirstDisplay());
            } else {
                spec.addEmbed(MessageUtils.getEmbed(gameTitle, getFirstDisplay(), Color.DISCORD_WHITE));
            }
            spec.setComponents(getComponents());
        }).block();
    }

    protected final void tie(String tieMessage) {
        end();
        setGameDisplay(tieMessage, DiscordColor.ORANGE);
    }

    protected void end() {
        active = false;
        if (Thread.currentThread().getId() != idleTimerThread.getId()) { //don't interrupt, just let it end
            idleTimerThread.interrupt();
        }
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

    protected final void registerComponentListener() {
        channel.getClient().on(ComponentInteractEvent.class)
                .takeUntil(e -> !active)
                .filter(e -> e.getMessageId().equals(gameMessage.getId()) && playerIsInGame(e.getInteraction().getMember().get()))
                .subscribe(event -> {
                    componentEvent = event;
                    onPlayerInput(componentEvent.getCustomId(), event.getInteraction().getMember().get());
                });
    }

    protected final void registerSelectMenuListener() {
        channel.getClient().on(SelectMenuInteractEvent.class)
                .takeUntil(e -> !active)
                .filter(e -> e.getMessageId().equals(gameMessage.getId()) && playerIsInGame(e.getInteraction().getMember().get()))
                .subscribe(event -> {
                    componentEvent = event;
                    onPlayerInput(event.getValues().get(0), event.getInteraction().getMember().get());
                });
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
        if (componentEvent != null) {
            componentEvent.edit(spec -> {
                if (!useEmbed()) {
                    spec.setContent(outside + embedText);
                } else {
                    spec.setContent(outside);
                    spec.addEmbed(MessageUtils.getEmbed(gameTitle, embedText, color));
                }
                if (!active) {
                    spec.setComponents();
                } else {
                    spec.setComponents(getComponents());
                }
            }).doOnError(Throwable::printStackTrace).onErrorResume(e -> Mono.empty()).block();
            componentEvent = null;
        } else {
            gameMessage.edit(spec -> {
                if (!useEmbed()) {
                    spec.setContent(outside + embedText);
                } else {
                    spec.setContent(outside);
                    spec.addEmbed(MessageUtils.getEmbed(gameTitle, embedText, color));
                }
                if (!active) {
                    spec.setComponents();
                } else {
                    spec.setComponents(getComponents());
                }
            }).block();
        }
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
