package discord.core.game;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import discord.data.UserManager;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.entity.User;
import discord4j.core.object.presence.Status;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import discord4j.core.object.entity.Message;
import discord.util.MessageUtils;

public class GameManager {

    private static final HashMap<Message, GameRequest> REQUESTS = new HashMap<>();
    private static final HashMap<Message, AbstractGame> GAMES = new HashMap<>();

    public static final int EARN_LIMIT = 1000;
    public static HashMap<Long, Integer> usersMoneyEarned = new HashMap<>();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    static {
        loadEarningLimits();

        //TODO rewrite this without if statements (it has to be possible)
        if (LocalDateTime.now().getHour() < 8) { //Schedule for this morning
            System.out.println("Scheduled for 8AM today");
            scheduler.scheduleAtFixedRate(usersMoneyEarned::clear,
                    LocalDateTime.now().until(LocalDate.now().atStartOfDay().plusHours(8), ChronoUnit.MINUTES),
                    TimeUnit.HOURS.toMinutes(24), TimeUnit.MINUTES);
        } else { //Schedule for tomorrow morning
            System.out.println("Scheduled for 8AM tomorrow");
            scheduler.scheduleAtFixedRate(usersMoneyEarned::clear,
                    LocalDateTime.now().until(LocalDate.now().plusDays(1).atStartOfDay().plusHours(8), ChronoUnit.MINUTES),
                    TimeUnit.HOURS.toMinutes(24), TimeUnit.MINUTES);
        }
    }

    private static void loadEarningLimits() {
        try {
            usersMoneyEarned = (HashMap<Long, Integer>) new ObjectMapper().readValue(new File("earning_limits.json"), new TypeReference<Map<Long, Integer>>() {});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveEarningLimits() {
        try {
            new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(new File("earning_limits.json"), usersMoneyEarned);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Collection<AbstractGame> getGames() {
        return GAMES.values();
    }

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
                game.handleMessageReaction(event.getEmoji(), event.getMember().get());
            } else if (REQUESTS.containsKey(event.getMessage().block())) {
                GameRequest request = REQUESTS.get(event.getMessage().block());
                request.handleMessageReaction(event.getEmoji(), event.getMember().get());
            }
        }
    }

    public static void processGameCommand(Message message, TextChannel channel, String[] args, String gameName, Class<? extends AbstractGame> gameType) {
        List<User> opponentList = message.getUserMentions().onErrorResume(e -> Flux.empty()).collectList().block();
        if (opponentList.isEmpty()) {
            MessageUtils.sendErrorMessage(channel, "Could not parse a valid opponent. Please @mention them.");
            return;
        }

        if (opponentList.get(0).isBot() || opponentList.get(0).equals(message.getAuthor().get())) {
            MessageUtils.sendErrorMessage(channel, "You can't play against yourself or a bot.");
            return;
        }

        Member player = message.getAuthorAsMember().block();
        Member opponent = opponentList.get(0).asMember(message.getGuild().block().getId()).block();

        if (opponentList.get(0).asMember(message.getGuild().block().getId()).block().getPresence().block().getStatus().equals(Status.OFFLINE)) {
            MessageUtils.sendErrorMessage(channel, "Your opponent must be online.");
            return;
        }

        int betAmount = 0;
        if (args.length > 1) {
            if (!args[1].matches("\\d+")) { //check for an int >= 0
                MessageUtils.sendErrorMessage(channel, "Couldn't parse a valid bet amount.");
                return;
            }

            betAmount = Integer.parseInt(args[1]); //TODO will break with numbers higher than int?
            if (betAmount > UserManager.getDUserFromMember(player).getBalance()) {
                MessageUtils.sendErrorMessage(channel, "You don't have that much money to bet!");
                return;
            } else if (betAmount > UserManager.getDUserFromMember(opponent).getBalance()) {
                MessageUtils.sendErrorMessage(channel, "Your opponent doesn't have that much money to bet!");
                return;
            }
        }

        Message requestMessage = channel.createEmbed(embed -> embed.setDescription("Creating request..")).block();
        GameManager.addGameRequest(requestMessage, new GameRequest(gameName, gameType, betAmount, requestMessage,
                new Member[] {player, opponent}));
    }
}

