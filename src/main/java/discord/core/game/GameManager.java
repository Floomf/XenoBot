package discord.core.game;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import discord.data.UserManager;
import discord.listener.EventsHandler;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.presence.Status;

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

    private static final Set<GameRequest> REQUESTS = new HashSet<>();
    private static final Set<AbstractGame> GAMES = new HashSet<>();

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

    public static Set<AbstractGame> getGames() {
        return GAMES;
    }

    public static boolean gameRequestExists(GameRequest request) {
        return REQUESTS.contains(request);
    }

    public static void addGame(AbstractGame game) {
        GAMES.add(game);
    }

    public static void removeGame(AbstractGame game) {
        GAMES.remove(game);
    }

    public static void removeGameRequest(GameRequest request) {
        REQUESTS.remove(request);
    }

    private static boolean playerIsInAGame(Member player) {
        return GAMES.stream().anyMatch(g -> g.playerIsInGame(player));
    }

    public static void createSinglePlayerGame(Class<? extends AbstractGame> gameType, TextChannel channel, Member player, int betAmount) {
        if (playerIsInAGame(player)) {
            MessageUtils.sendErrorMessage(channel, "You're already in a game!");
            return;
        }

        try {
            Message gameMessage = channel.createEmbed(embed -> embed.setDescription("Loading..")).block();
            AbstractGame game = gameType.getConstructor( //dont know how else to do this?
                    Message.class, Member[].class, int.class).newInstance(gameMessage, new Member[]{player}, betAmount);
            addGame(game);
            game.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void createMultiPlayerGame(Class<? extends AbstractGame> gameType, String gameName, TextChannel channel, Message message, String[] args) {
        Member player = message.getAuthorAsMember().block();

        if (REQUESTS.stream().anyMatch(r -> r.isCreatedBy(player))) {
            MessageUtils.sendErrorMessage(channel, "You've already created a pending game request!");
            return;
        } else if (playerIsInAGame(player)) {
            MessageUtils.sendErrorMessage(channel, "You're already in a game!");
            return;
        }

        Member opponent = message.getUserMentions()
                .filter(user -> !(user.isBot() || user.equals(message.getAuthor().orElse(null))))
                .flatMap(u -> u.asMember(channel.getGuildId()))
                .blockFirst();

        if (opponent == null) {
            MessageUtils.sendErrorMessage(channel, "Couldn't parse a playable opponent. Please @mention them.");
            return;
        }

        if (opponent.getPresence().block().getStatus().equals(Status.OFFLINE)) {
            MessageUtils.sendErrorMessage(channel, "Your opponent must be online.");
            return;
        }

        int betAmount = 0;
        if (channel.getGuildId().equals(EventsHandler.THE_REALM_ID) && args.length > 1) {
            if (!args[1].matches("\\d{1,9}")) { //check for an int >= 0
                MessageUtils.sendErrorMessage(channel, "Couldn't parse a valid bet amount.");
                return;
            }

            betAmount = Integer.parseInt(args[1]);
            if (betAmount > UserManager.getDUserFromMember(player).getBalance()) {
                MessageUtils.sendErrorMessage(channel, "You don't have that much money to bet!");
                return;
            } else if (betAmount > UserManager.getDUserFromMember(opponent).getBalance()) {
                MessageUtils.sendErrorMessage(channel, "Your opponent doesn't have that much money to bet!");
                return;
            }
        }

        Message requestMessage = channel.createEmbed(embed -> embed.setDescription("Creating request..")).block();
        REQUESTS.add(new GameRequest(gameName, gameType, betAmount, requestMessage, new Member[] {player, opponent}));
    }
}

