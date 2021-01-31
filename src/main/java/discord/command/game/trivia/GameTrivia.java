package discord.command.game.trivia;

import discord.core.game.GameEmoji;
import discord.core.game.MultiplayerGame;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class GameTrivia extends MultiplayerGame {

    private static final int QUESTION_COUNT = 15;
    private static String TOKEN = getNewToken();
    private static final TriviaCategory[] CATEGORIES = new TriviaCategory[] {
            new TriviaCategory("Everything", 0),
            new TriviaCategory("General Knowledge", 9),
            new TriviaCategory("Books", 10),
            new TriviaCategory("Films", 11),
            new TriviaCategory("Music", 12),
            new TriviaCategory("Television", 14),
            new TriviaCategory("Video Games", 15),
            new TriviaCategory("Board Games", 16),
            new TriviaCategory("Science & Nature", 17),
            new TriviaCategory("Computer Science", 18),
            new TriviaCategory("Mathematics", 19),
            new TriviaCategory("Mythology", 20),
            new TriviaCategory("Sports", 21),
            new TriviaCategory("Geography", 22),
            new TriviaCategory("History", 23),
            new TriviaCategory("Politics", 24),
            new TriviaCategory("Celebrities", 26),
            new TriviaCategory("Animals", 27),
            new TriviaCategory("Vehicles", 28),
            new TriviaCategory("Comics", 29),
            new TriviaCategory("Anime & Manga", 31),
            new TriviaCategory("Cartoons & Animations", 32),
    };

    private int selectedCategoryIndex = -1;
    private long selectedCategoryPlayerID = -1;
    private TriviaCategory[] shuffledCategories;

    private final String[] currentChoices = new String[4];
    private JSONArray questionsJSON;
    private JSONObject currentQuestionJSON;

    private final List<Long> guessedPlayers = new ArrayList<>();
    private Timer questionTimer = new Timer();
    private Timer cooldownTimer = new Timer();

    private final HashMap<Member, Integer> scoresMap = new HashMap<>();

    private boolean cooldown = false;

    public GameTrivia(String gameTitle, TextChannel channel, Member[] players, int betAmount) {
        super(gameTitle, channel, players, betAmount);
    }

    @Override
    protected String getForfeitMessage(Member forfeiter) {
        return forfeiter.getMention() + " forfeited.\n" + super.getOtherPlayer(forfeiter).getMention() + " wins!";
    }

    @Override
    protected String getIdleMessage(Member idler) {
        return idler.getMention() + " failed to go in time.\n" + super.getOtherPlayer(idler).getMention() + " wins!";
    }

    @Override
    protected void setup() {
        scoresMap.put(getPThisTurn(), 0);
        scoresMap.put(getPNextTurn(), 0);
        shuffledCategories = getShuffledCategories();
    }

    @Override
    protected String getFirstDisplay() {
        return "First, agree on a **category**!\nReact with your choice below.\n\n" + getCategories();
    }

    @Override
    protected void onStart() {
        super.registerReactionListener();
        super.addEmojiReaction(GameEmoji.A);
        super.addEmojiReaction(GameEmoji.B);
        super.addEmojiReaction(GameEmoji.C);
        super.addEmojiReaction(GameEmoji.D);
    }

    private static String getNewToken() {
        return Unirest.get("https://opentdb.com/api_token.php?command=request")
                .asJson().getBody().getObject().getString("token");
    }

    private static TriviaCategory[] getShuffledCategories() {
        TriviaCategory[] categories = Arrays.copyOf(CATEGORIES, CATEGORIES.length);
        Random random = new Random();
        int index;
        TriviaCategory temp;
        for (int i = categories.length - 1; i > 0; i--)
        {
            index = random.nextInt(i + 1);
            temp = categories[index];
            categories[index] = categories[i];
            categories[i] = temp;
        }
        return categories;
    }

    private void generateQuestions(TriviaCategory category) {
        JSONObject response = Unirest.get("https://opentdb.com/api.php?amount=" + QUESTION_COUNT 
                + "&category=" + category.getID() + "&type=multiple&token=" + TOKEN)
                .asJson().getBody().getObject();

        if (response.getInt("response_code") == 3 || response.getInt("response_code") == 4) {
            LoggerFactory.getLogger(getClass()).info("Trivia token invalid/exhausted! Fetching new one.");
            TOKEN = getNewToken();
            generateQuestions(category);
            return;
        }

        questionsJSON = response.getJSONArray("results");
    }

    private String decode(String input) {
        return StringEscapeUtils.unescapeHtml4(input);
    }

    private int getIndexFromLetterEmoji(String emoji) {
        if (emoji.length() != 2) {
            return -1;
        }
        return emoji.charAt(1) - '\uDDE6';
    }

    private String getGuessFromEmoji(String emoji) {
        return currentChoices[getIndexFromLetterEmoji(emoji)];
    }

    private void setupNextQuestion() {
        currentQuestionJSON = questionsJSON.getJSONObject(super.getTurn() - 1);

        int start = (int) (Math.random() * 4);
        currentChoices[start] = decode(currentQuestionJSON.getString("correct_answer"));
        for (int i = 0; i < 3; i++) {
            start++;
            if (start == currentChoices.length) {
                start = 0;
            }
            currentChoices[start] = decode(currentQuestionJSON.getJSONArray("incorrect_answers").getString(i));
        }
    }

    @Override
    protected void setupNextTurn() {
        super.setupNextTurn();
        if (getTurn() > questionsJSON.length()) {
            findWinner();
        } else {
            guessedPlayers.clear();
            setupNextQuestion();
            startCooldownTimer();
        }
    }

    private void startCooldownTimer() {
        cooldown = true;
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                cooldown = false;
                cooldownTimer.cancel();
            }
        };
        cooldownTimer.cancel();
        cooldownTimer = new Timer();
        cooldownTimer.schedule(task, 1250);
    }

    private void startQuestionTimer() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (getTurn() == questionsJSON.length()) {
                    findWinner();
                } else {
                    setupNextTurn();
                    GameTrivia.super.setInfoDisplay("⌛ Time's up! Next question:");
                }

            }
        };
        questionTimer.cancel();
        questionTimer = new Timer();
        questionTimer.scheduleAtFixedRate(task, TimeUnit.SECONDS.toMillis(20), TimeUnit.SECONDS.toMillis(20));
    }

    private void findWinner() {
        questionTimer.cancel();
        if (scoresMap.get(getPThisTurn()) > scoresMap.get(getPNextTurn())) {
            win("🏆 " + getPThisTurn().getMention() + " wins!\n\n" + getResults(getPThisTurn(), getPNextTurn()), getPThisTurn());
        } else if (scoresMap.get(getPThisTurn()).equals(scoresMap.get(getPNextTurn()))) {
            super.tie("Even scores! Tie!\n\n" + getResults(getPNextTurn(), getPThisTurn()));
        } else {
            win("🏆 " + getPNextTurn().getMention() + " wins!\n\n" + getResults(getPNextTurn(), getPThisTurn()), getPNextTurn());
        }
    }

    private String formatScores(Member player1, Member player2) {
        return "**" + scoresMap.get(player1) + "** ― " + player1.getMention()
                + "\n**" + scoresMap.get(player2) + "** ― " + player2.getMention();
    }

    private String getResults(Member player1, Member player2) {
        return ":label: __**Category:**__:\n" + shuffledCategories[selectedCategoryIndex].getName()
                + "\n\n:bar_chart: __**Scores:**__\n" + formatScores(player1, player2);
    }

    @Override //TODO REDESIGN AGAIN
    protected void onTurn(String input) {};

    protected void onTurn(String input, Member guessingPlayer) {
        //if right, add point, next question
        if (getGuessFromEmoji(input).equals(decode(currentQuestionJSON.getString("correct_answer")))) {
            scoresMap.put(guessingPlayer, scoresMap.get(guessingPlayer) + 1);
            if (getTurn() == questionsJSON.length()) {
                findWinner();
            } else {
                setupNextTurn();
                startQuestionTimer();
                super.setInfoDisplay("✅ " + guessingPlayer.getMention() + " is correct! Next question:");
            }
        } else {
            guessedPlayers.add(guessingPlayer.getId().asLong());
            if (guessedPlayers.size() == scoresMap.keySet().size()) {
                if (getTurn() == questionsJSON.length()) {
                    findWinner();
                    questionTimer.cancel();
                } else {
                    setupNextTurn();
                    startQuestionTimer();
                    super.setInfoDisplay("❌ Nobody got it! Next question:");
                }
            } else {
                super.setInfoDisplay("❌ " + guessingPlayer.getMention()+ " is incorrect!");
            }
        }
    }

    @Override
    public void onPlayerReaction(ReactionEmoji emoji, Member player) {
        String raw = emoji.asUnicodeEmoji().map(ReactionEmoji.Unicode::getRaw).orElse("");

        if (raw.equals(GameEmoji.EXIT)) {
            questionTimer.cancel();
            win(getForfeitMessage(player), getOtherPlayer(player));
        } else if (questionsJSON == null) { //still selecting category
            int index = getIndexFromLetterEmoji(raw);
            if (index < 0 || index >= 4) {
                return;
            }

            if (index == selectedCategoryIndex && player.getId().asLong() != selectedCategoryPlayerID) {
                generateQuestions(shuffledCategories[index]);
                setupNextQuestion();
                super.setInfoDisplay("Start! Answer by clicking below.");
                startQuestionTimer();
            } else {
                selectedCategoryIndex = index;
                selectedCategoryPlayerID = player.getId().asLong();
                super.setGameDisplay(player.getMention() + " chose **" + shuffledCategories[index].getName() + "**!"
                        + "\nIf you agree " + super.getOtherPlayer(player).getMention()
                        + ", select it as well.\n\n" + getCategories());
            }
        } else if (isValidInput(raw, player)) {
            onTurn(raw, player);
        }
    }

    @Override
    protected boolean isValidInput(String input) {return true;}

    protected boolean isValidInput(String input, Member guessingPlayer) {
        return !cooldown && getIndexFromLetterEmoji(input) >= 0 && getIndexFromLetterEmoji(input) < currentChoices.length
                && !guessedPlayers.contains(guessingPlayer.getId().asLong());
    }

    private String getCategories() {
        return GameEmoji.A + " " + shuffledCategories[0].getName()
                + "\n" + GameEmoji.B + " " + shuffledCategories[1].getName()
                + "\n" + GameEmoji.C + " " + shuffledCategories[2].getName()
                + "\n" + GameEmoji.D + " " + shuffledCategories[3].getName();
    }

    @Override
    protected String getBoard() {
        return "**" + decode(currentQuestionJSON.getString("question")) + "**\n"
                + "\n" + GameEmoji.A + " " + currentChoices[0]
                + "\n" + GameEmoji.B + " " + currentChoices[1]
                + "\n" + GameEmoji.C + " " + currentChoices[2]
                + "\n" + GameEmoji.D + " " + currentChoices[3];
    }

}
