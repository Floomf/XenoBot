package discord.core.game;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import discord.core.command.InteractionContext;
import discord.manager.GameManager;
import discord.util.DiscordColor;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import discord.util.MessageUtils;
import discord4j.core.spec.WebhookEditSpec;
import discord4j.discordjson.json.WebhookMessageEditRequest;

public class GameRequest {

    private final String gameTitle;
    private final Class<? extends BaseGame> gameType;
    private final int betAmount;
    private final TextChannel channel;
    private final Member[] players;
    private final Timer inactiveTimer;

    private Message requestMessage;
    private InteractionContext context = null;

    public GameRequest(String gameTitle, Class<? extends BaseGame> gameType, TextChannel channel, Member[] players, int betAmount) {
        this.gameTitle = gameTitle;
        this.gameType = gameType;
        this.betAmount = betAmount;
        this.channel = channel;
        this.players = players;
        this.inactiveTimer = new Timer();
    }

    private void setup() {
        requestMessage.addReaction(ReactionEmoji.unicode(GameEmoji.CHECKMARK)).block();
        requestMessage.addReaction(ReactionEmoji.unicode(GameEmoji.EXIT)).block();

        inactiveTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                requestMessage.edit(spec -> spec.setEmbed(MessageUtils.getEmbed("Game Request",
                        players[1].getMention() + " failed to respond to the request in time."))).block();
                requestMessage.removeAllReactions().block();
                remove();
            }
        }, TimeUnit.SECONDS.toMillis(30));

        requestMessage.getClient().on(ReactionAddEvent.class).takeWhile(e -> GameManager.gameRequestExists(this))
                .filter(e -> e.getMessageId().equals(requestMessage.getId()))
                .doOnNext(e -> requestMessage.removeReaction(e.getEmoji(), e.getUserId()))
                .filter(e -> players[1].equals(e.getMember().get()))
                .map(ReactionAddEvent::getEmoji)
                .subscribe(this::onOpponentReaction);
    }

    public void create() {
        requestMessage = channel.createMessage(spec -> {
            spec.setContent(players[1].getMention());
            if (betAmount > 0) {
                spec.setEmbed(MessageUtils.getEmbed("Game Request",
                        players[0].getMention() + " has challenged you to a money match of **" + gameTitle
                                + "**.\n\nYou'll each bet **$" + betAmount + "**. You have 30 seconds to accept/deny."));
            } else {
                spec.setEmbed(MessageUtils.getEmbed("Game Request",
                        players[0].getMention() + " has challenged you to a game of **" + gameTitle + "**.\n\nYou have 30 seconds to accept/deny."));
            }
        }).block();
        setup();
    }

    public void createFromInteraction(InteractionContext context) {
        context.getChannel().getClient().on(MessageCreateEvent.class)
                .map(MessageCreateEvent::getMessage)
                .filter(message -> message.getInteraction().map(interaction -> interaction.getId().equals(context.event.getInteraction().getId())).orElse(false))
                .take(1)
                .subscribe(message -> {
                    requestMessage = message;
                    setup();
                });
        if (betAmount > 0) {
            context.reply(players[1].getMention(), MessageUtils.getEmbed("Game Request",
                    players[0].getMention() + " has challenged you to a money match of **" + gameTitle
                            + "**.\n\nYou'll each bet **$" + betAmount + "**. You have 30 seconds to accept/deny."));
        } else {
            context.reply(players[1].getMention(), MessageUtils.getEmbed("Game Request",
                    players[0].getMention() + " has challenged you to a game of **" + gameTitle + "**.\n\nYou have 30 seconds to accept/deny."));
        }
        this.context = context;
    }

    private void onOpponentReaction(ReactionEmoji reaction) {
        String emoji = reaction.asUnicodeEmoji().map(ReactionEmoji.Unicode::getRaw).orElse("");
        if (emoji.equals(GameEmoji.CHECKMARK)) {
            createGame();
            inactiveTimer.cancel();
            remove();
        } else if (emoji.equals(GameEmoji.EXIT)) {
            requestMessage.removeAllReactions().block();
            requestMessage.edit(spec -> spec.setContent(players[0].getMention()).setEmbed(
                    MessageUtils.getEmbed("Game Request", players[1].getMention() + " has denied your request.",
                            DiscordColor.RED))).block();
            inactiveTimer.cancel();
            remove();
        }
    }

    public boolean isCreatedBy(Member player) {
        return players[0].equals(player);
    }

    public void createGame() {
        try {
            BaseGame game = gameType.getConstructor(String.class, TextChannel.class, Member[].class, int.class)
                    .newInstance(gameTitle, channel, players, betAmount);
            GameManager.addGame(game);
            if (context != null) {
                requestMessage.removeAllReactions().block();
                context.event.getInteractionResponse().editInitialResponse(WebhookMessageEditRequest.builder().embeds(new ArrayList<>()).build()).block();
                game.startFromMessage(requestMessage);
            } else {
                requestMessage.delete().block();
                game.start();
            }
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
    }

    private void remove() {
        GameManager.removeGameRequest(this);
    }

}

