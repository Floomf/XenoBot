package discord.manager;

import discord.core.command.InteractionContext;
import discord.core.game.BaseGame;
import discord.core.game.GameRequest;
import discord.listener.EventsHandler;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.presence.Status;
import discord4j.core.object.entity.Message;
import discord.util.MessageUtils;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.discordjson.json.ImmutableApplicationCommandRequest;
import discord4j.rest.util.ApplicationCommandOptionType;

import java.util.*;

public class GameManager {

    private static final Set<GameRequest> REQUESTS = new HashSet<>();
    private static final Set<BaseGame> GAMES = new HashSet<>();

    public static Set<BaseGame> getGames() {
        return GAMES;
    }

    public static boolean gameRequestExists(GameRequest request) {
        return REQUESTS.contains(request);
    }

    public static void addGame(BaseGame game) {
        GAMES.add(game);
    }

    public static void removeGame(BaseGame game) {
        GAMES.remove(game);
    }

    public static void removeGameRequest(GameRequest request) {
        REQUESTS.remove(request);
    }

    public static boolean playerIsInTypingGame(Member player) {
        return GAMES.stream().anyMatch(g -> g.isTypingGame() && g.playerIsInGame(player));
    }

    private static boolean playerIsInAGame(Member player) {
        return GAMES.stream().anyMatch(g -> g.playerIsInGame(player));
    }

    public static void createSinglePlayerGame(Class<? extends BaseGame> gameType, String gameTitle, InteractionContext context, int betAmount) {
        if (playerIsInAGame(context.getMember())) {
            context.replyWithError("You're already in a game!");
            return;
        }

        //context.acknowledge();

        try {
            BaseGame game = gameType.getConstructor(String.class, TextChannel.class, Member.class, int.class)
                    .newInstance(gameTitle, context.getChannel(), context.getMember(), betAmount);
            addGame(game);
            game.startFromInteraction(context);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void createSinglePlayerGame(Class<? extends BaseGame> gameType, String gameTitle, TextChannel channel, Member player, int betAmount) {
        if (playerIsInAGame(player)) {
            MessageUtils.sendErrorMessage(channel, "You're already in a game!");
            return;
        }

        try {
            BaseGame game = gameType.getConstructor(String.class, TextChannel.class, Member.class, int.class)
                    .newInstance(gameTitle, channel, player, betAmount);
            addGame(game);
            game.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void createMultiPlayerGame(Class<? extends BaseGame> gameType, String gameTitle, InteractionContext context) {
        Member player = context.getMember();

        if (REQUESTS.stream().anyMatch(r -> r.isCreatedBy(player))) {
            context.replyWithError("You've already created a pending game request!");
            return;
        } else if (playerIsInAGame(player)) {
            context.replyWithError("You're already in a game!");
            return;
        }

        Optional<Member> opponent = context.getOptionAsMember("opponent")
                .filter(user -> !(user.isBot() || user.equals(player)));

        if (opponent.isEmpty()) {
            context.replyWithError( "You can't play against yourself or any bots!");
            return;
        }

        long bet = context.getOptionAsLong("bet").orElse(0L);
        if (bet > 0L) {
            if (bet > UserManager.getDUserFromMember(player).getBalance()) {
                context.replyWithError( "You don't have that much money to bet!");
                return;
            } else if (bet > UserManager.getDUserFromMember(opponent.get()).getBalance()) {
                context.replyWithError( "Your opponent doesn't have that much money to bet!");
                return;
            }
        }

        GameRequest request = new GameRequest(gameTitle, gameType, context.getChannel(), new Member[] {player, opponent.get()}, (int) bet);
        REQUESTS.add(request);
        request.create(context);
    }

    public static void createMultiPlayerGame(Class<? extends BaseGame> gameType, String gameTitle, TextChannel channel, Message message, String[] args) {
        /*Member player = message.getAuthorAsMember().block();

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

        GameRequest request = new GameRequest(gameTitle, gameType, channel, new Member[] {player, opponent}, betAmount);
        REQUESTS.add(request);
        request.create();*/
    }

    public static ApplicationCommandRequest buildMultiplayerGameSlashCommand(String commandName, String gameTitle, boolean canBet) {
        ImmutableApplicationCommandRequest.Builder request = ApplicationCommandRequest.builder()
                .name(commandName)
                .description("Play a game of " + gameTitle + " with someone")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("opponent")
                        .description("Your opponent")
                        .type(ApplicationCommandOptionType.USER.getValue())
                        .required(true)
                        .build());
        if (canBet) {
            request.addOption(ApplicationCommandOptionData.builder()
                    .name("bet")
                    .description("The bet (Type 0 for no bet)")
                    .type(ApplicationCommandOptionType.INTEGER.getValue())
                    .required(true)
                    .build())
                    .build();
        }
        return request.build();
    }
}

