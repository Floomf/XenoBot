package discord.core.game;

import java.lang.reflect.InvocationTargetException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;
import discord.util.MessageUtils;

public class GameRequest {

    private final String gameName;
    private final Class<? extends AbstractGame> gameType;
    private final int betAmount;
    private final Message requestMessage;
    private final Member[] players;
    private final ButtonManager bm;
    private final Timer inactiveTimer;

    public GameRequest(String gameName, Class<? extends AbstractGame> gameType, int betAmount,
                       Message message, Member[] players) {
        this.gameName = gameName;
        this.gameType = gameType;
        this.betAmount = betAmount;
        this.requestMessage = message;
        this.players = players;
        this.bm = new ButtonManager();
        this.inactiveTimer = new Timer();
        setup();
    }

    private void setup() {
        bm.addButton(requestMessage, Button.CHECKMARK);
        bm.addButton(requestMessage, Button.EXIT);

        requestMessage.edit(spec -> {
            spec.setContent(players[1].getMention());
            if (betAmount > 0) {
                spec.setEmbed(MessageUtils.getEmbed("Game Request",
                        players[0].getMention() + " has challenged you to a money match of `" + gameName
                                + "`.\n\nYou'll each bet **$" + betAmount + "**. Do you accept?"));
            } else {
                spec.setEmbed(MessageUtils.getEmbed("Game Request",
                        players[0].getMention() + " has challenged you to a game of `" + gameName + "`.\n\nDo you accept?"));
            }
        }).block();

        inactiveTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                requestMessage.edit(spec -> spec.setEmbed(MessageUtils.getEmbed("Game Request",
                        players[1].getMention() + " failed to respond to the request in time."))).block();
                requestMessage.removeAllReactions().block();
                remove();
            }
        }, TimeUnit.MINUTES.toMillis(3));

        requestMessage.getClient().on(ReactionAddEvent.class).takeWhile(e -> GameManager.gameRequestExists(this))
                .filter(e -> e.getMessageId().equals(requestMessage.getId()))
                .doOnNext(e -> requestMessage.removeReaction(e.getEmoji(), e.getUserId()))
                .filter(e -> players[1].equals(e.getMember().get()))
                .map(ReactionAddEvent::getEmoji)
                .subscribe(this::onOpponentReaction);
    }

    private void onOpponentReaction(ReactionEmoji reaction) {
        Button button = bm.getButton(reaction);
        if (button != null) {
            requestMessage.removeAllReactions().block();
            if (button.equals(Button.CHECKMARK)) {
                createGame();
            } else if (button.equals(Button.EXIT)) {
                requestMessage.edit(spec -> spec.setContent(players[0].getMention()).setEmbed(
                        MessageUtils.getEmbed("Game Request", players[1].getMention() + " has denied your request."))).block();
            }
            remove();
            inactiveTimer.cancel();
        }
    }

    public boolean isCreatedBy(Member player) {
        return players[0].equals(player);
    }

    public void createGame() {
        try {
            requestMessage.edit(spec ->  {
                spec.setContent("");
                spec.setEmbed(embed -> embed.setDescription("Loading `" + gameName + "`.."));
            }).block();
            AbstractGame game = gameType.getConstructor( //dont know how else to do this?
                    Message.class, Member[].class, int.class).newInstance(requestMessage, players, betAmount);
            GameManager.addGame(game);
            game.start();
        } catch (NoSuchMethodException
                | InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
    }

    private void remove() {
        GameManager.removeGameRequest(this);
    }

}

