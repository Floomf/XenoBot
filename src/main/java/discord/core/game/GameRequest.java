package discord.core.game;

import java.lang.reflect.InvocationTargetException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import discord.manager.GameManager;
import discord.util.DiscordColor;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import discord.util.MessageUtils;

public class GameRequest {

    private final String gameTitle;
    private final Class<? extends BaseGame> gameType;
    private final int betAmount;
    private final TextChannel channel;
    private final Member[] players;
    private final Timer inactiveTimer;

    private Message requestMessage;

    public GameRequest(String gameTitle, Class<? extends BaseGame> gameType, TextChannel channel, Member[] players, int betAmount) {
        this.gameTitle = gameTitle;
        this.gameType = gameType;
        this.betAmount = betAmount;
        this.channel = channel;
        this.players = players;
        this.inactiveTimer = new Timer();
        setup();
    }

    private void setup() {
        requestMessage = channel.createMessage(spec -> {
            spec.setContent(players[1].getMention());
            if (betAmount > 0) {
                spec.setEmbed(MessageUtils.getEmbed("Game Request",
                        players[0].getMention() + " has challenged you to a money match of **" + gameTitle
                                + "**.\n\nYou'll each bet **$" + betAmount + "**. Do you accept?"));
            } else {
                spec.setEmbed(MessageUtils.getEmbed("Game Request",
                        players[0].getMention() + " has challenged you to a game of **" + gameTitle + "**.\n\nDo you accept?"));
            }
        }).block();

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
        }, TimeUnit.SECONDS.toMillis(45));

        requestMessage.getClient().on(ReactionAddEvent.class).takeWhile(e -> GameManager.gameRequestExists(this))
                .filter(e -> e.getMessageId().equals(requestMessage.getId()))
                .doOnNext(e -> requestMessage.removeReaction(e.getEmoji(), e.getUserId()))
                .filter(e -> players[1].equals(e.getMember().get()))
                .map(ReactionAddEvent::getEmoji)
                .subscribe(this::onOpponentReaction);
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
            requestMessage.delete().block();
            BaseGame game = gameType.getConstructor(String.class, TextChannel.class, Member[].class, int.class)
                    .newInstance(gameTitle, channel, players, betAmount);
            GameManager.addGame(game);
            game.start();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
    }

    private void remove() {
        GameManager.removeGameRequest(this);
    }

}

