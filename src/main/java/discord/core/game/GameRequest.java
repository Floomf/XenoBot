package discord.core.game;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import discord.core.command.InteractionContext;
import discord.manager.GameManager;
import discord.util.DiscordColor;
import discord4j.core.event.domain.interaction.ButtonInteractEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.component.LayoutComponent;
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

    public GameRequest(String gameTitle, Class<? extends BaseGame> gameType, TextChannel channel, Member[] players, int betAmount) {
        this.gameTitle = gameTitle;
        this.gameType = gameType;
        this.betAmount = betAmount;
        this.channel = channel;
        this.players = players;
        this.inactiveTimer = new Timer();
    }

    private void setup() {
        inactiveTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                remove();
                requestMessage.edit(spec -> {
                    spec.addEmbed(MessageUtils.getEmbed("Game Request",
                            players[1].getMention() + " failed to respond to the request in time."));
                    spec.setComponents();
                }).block();
            }
        }, TimeUnit.SECONDS.toMillis(60));

        requestMessage.getClient().on(ButtonInteractEvent.class).takeWhile(e -> GameManager.gameRequestExists(this))
                .filter(e -> e.getMessage().equals(requestMessage))
                .filter(e -> players[1].equals(e.getInteraction().getMember().get()))
                .subscribe(this::onOpponentButton);
    }

    public void create(InteractionContext context) {
        context.getChannel().getClient().on(MessageCreateEvent.class)
                .map(MessageCreateEvent::getMessage)
                .filter(message -> message.getInteraction().map(interaction -> interaction.getId().equals(context.event.getInteraction().getId())).orElse(false))
                .take(1)
                .subscribe(message -> {
                    requestMessage = message;
                    setup();
                });
        LayoutComponent[] components = {ActionRow.of(Button.success("accept", "Accept"),
                Button.danger("deny", "Deny"))};
        if (betAmount > 0) {
            context.reply(players[1].getMention(), MessageUtils.getEmbed("Game Request",
                    players[0].getMention() + " has challenged you to a money match of **" + gameTitle
                            + "**.\n\nYou'll each bet **$" + betAmount + "**. You have 60 seconds to respond."),
                    components);
        } else {
            context.reply(players[1].getMention(), MessageUtils.getEmbed("Game Request",
                    players[0].getMention() + " has challenged you to a game of **" + gameTitle + "**.\n\nYou have 60 seconds to respond."),
                    components);
        }
    }

    private void onOpponentButton(ButtonInteractEvent event) {
        String response = event.getCustomId();
        if (response.equals("accept")) {
            createGame();
            inactiveTimer.cancel();
            remove();
        } else if (response.equals("deny")) {
            requestMessage.edit(spec -> {
                spec.setContent(players[0].getMention());
                spec.addEmbed(MessageUtils.getEmbed("Game Request",
                        players[1].getMention() + " denied your request.",
                                DiscordColor.RED));
                spec.setComponents();
            }).block();
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
            requestMessage.edit(spec -> {
                spec.setEmbed(null);
                spec.setComponents();
            }).block();
            game.startFromMessage(requestMessage);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
    }

    private void remove() {
        GameManager.removeGameRequest(this);
    }

}

