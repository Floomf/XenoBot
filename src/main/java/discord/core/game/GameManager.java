package discord.core.game;

import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.entity.User;
import discord4j.core.object.presence.Status;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;

import discord4j.core.object.entity.Message;
import discord.util.MessageUtils;

public class GameManager {

    private static final HashMap<Message, GameRequest> REQUESTS = new HashMap<>();
    private static final HashMap<Message, AbstractGame> GAMES = new HashMap<>();

    public static void addGameRequest(Message requestMessage, GameRequest request) {
        REQUESTS.put(requestMessage, request);
    }

    public static void addGame(Message gameMessage, AbstractGame game) {
        GAMES.put(gameMessage, game);
    }

    public static void removeGameRequest(Message requestMessage) {
        REQUESTS.remove(requestMessage);
    }

    public static void removeGame(Message gameMessage) {
        GAMES.remove(gameMessage);
    }

    public static void onReactionAddEvent(ReactionAddEvent event) {
        if (event.getGuild().onErrorResume(e -> Mono.empty()).block() != null && !event.getUser().block().isBot()) {

            if (GAMES.containsKey(event.getMessage().block()) && (GAMES.get(event.getMessage().block()) instanceof ButtonGame)) {
                ButtonGame game = (ButtonGame) GAMES.get(event.getMessage().block());
                game.handleMessageReaction(event.getEmoji(), event.getUser().block().asMember(event.getGuildId().get()).block());
            } else if (REQUESTS.containsKey(event.getMessage().block())) {
                GameRequest request = REQUESTS.get(event.getMessage().block());
                request.handleMessageReaction(event.getEmoji(), event.getUser().block().asMember(event.getGuildId().get()).block());
            }
        }
    }

    public static void processGameCommand(Message message, TextChannel channel, String gameName, Class<? extends AbstractGame> gameType) {
        List<User> opponentList = message.getUserMentions().onErrorResume(e -> Flux.empty()).collectList().block();
        ;
        if (opponentList.isEmpty()) {
            MessageUtils.sendErrorMessage(channel, "Could not parse a valid opponent. Please @mention them.");
            return;
        }

        if (opponentList.get(0).isBot() || opponentList.get(0).equals(message.getAuthor().get())) {
            MessageUtils.sendErrorMessage(channel, "You can't play against yourself or a bot.");
            return;
        }

        if (opponentList.get(0).asMember(message.getGuild().block().getId()).block().getPresence().block().getStatus().equals(Status.OFFLINE)) {
            MessageUtils.sendErrorMessage(channel, "Your opponent must be online.");
            return;
        }

        Message requestMessage = channel.createMessage("Loading request..").block();
        GameManager.addGameRequest(requestMessage, new GameRequest(gameName, gameType, requestMessage,
                new Member[]{message.getAuthorAsMember().block(),
                        opponentList.get(0).asMember(message.getGuild().block().getId()).block()}));
    }
}

