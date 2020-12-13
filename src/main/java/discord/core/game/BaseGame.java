package discord.core.game;

import discord.manager.GameManager;
import discord.util.BotUtils;
import discord.util.DiscordColor;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.rest.util.Color;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public abstract class BaseGame {

    private final String gameTitle;
    private final TextChannel channel;
    private Message gameMessage;
    private int betAmount;
    private Timer idleTimer;

    private boolean active;
    private int turn;

    public BaseGame(String gameTitle, TextChannel channel, int betAmount) {
        this.gameTitle = gameTitle;
        this.channel = channel;
        this.betAmount = betAmount;
        this.idleTimer = new Timer();
        this.active = false;
    }

    protected boolean useEmbed() {
        return true;
    }

    abstract protected String getFirstDisplay();

    //For setting up specific game types (button manager and message listener);
    abstract protected void setup();

    //Games are responsible for calling updateMessageDisplay() on start
    abstract protected void onStart();

    abstract protected void onEnd();

    abstract protected void onTurn(String input);

    abstract protected void onTimeout();

    //either a message, or a reaction's emoji
    abstract protected boolean isValidInput(String input);

    abstract protected String getBoard();

    public final void start() {
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
        startIdleTimer();
        onStart();
    }

    protected final void tie(String tieMessage) {
        setGameDisplay(tieMessage + "\n\n" + getBoard(), DiscordColor.ORANGE);
        end();
    }

    protected void end() {
        if (active) { //is this necessary?
            idleTimer.cancel();
            onEnd();
            active = false;
            GameManager.removeGame(this);
        }
    }

    protected final void registerMessageListener() {
        channel.getClient().getEventDispatcher().on(MessageCreateEvent.class)
                .takeUntil(e -> !active)
                .filter(e -> e.getMessage().getChannelId().equals(channel.getId()) && playerIsInGame(e.getMember().get()))
                .subscribe(e -> onPlayerMessage(e.getMessage(), e.getMember().get()));
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

    abstract protected void onPlayerMessage(Message message, Member player);

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
        startIdleTimer();
    }

    private void startIdleTimer() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                onTimeout();
            }
        };
        idleTimer.cancel();
        idleTimer = new Timer();
        idleTimer.schedule(task, TimeUnit.MINUTES.toMillis(3));
    }

}
