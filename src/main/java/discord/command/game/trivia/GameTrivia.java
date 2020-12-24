package discord.command.game.trivia;

import discord.core.game.GameEmoji;
import discord.core.game.MultiplayerGame;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class GameTrivia extends MultiplayerGame {

    private static final int QUESTION_COUNT = 15;

    private final String[] currentChoices = new String[4];
    private JSONArray questionsJSON;
    private JSONObject currentQuestionJSON;

    private final List<Long> guessedPlayers = new ArrayList<>();
    private Timer questionTimer = new Timer();
    private Timer cooldownTimer = new Timer();

    private final HashMap<Member, Integer> scoresMap = new HashMap<>();

    private boolean cooldown = false;

    //TODO REDEISGN
    private Member guessingPlayer;

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
        questionsJSON = Unirest.get("https://opentdb.com/api.php?amount=" + QUESTION_COUNT + "&difficulty=easy&type=multiple")
                .asJson().getBody().getObject().getJSONArray("results");

        scoresMap.put(getPThisTurn(), 0);
        scoresMap.put(getPNextTurn(), 0);
        setupNextQuestion();
    }

    @Override
    protected String getFirstDisplay() {
        return "Start! Answer by clicking below.\n\n" + getBoard();
    }

    @Override
    protected void onStart() {
        super.registerReactionListener();
        startQuestionTimer();
        super.addEmojiReaction(GameEmoji.A);
        super.addEmojiReaction(GameEmoji.B);
        super.addEmojiReaction(GameEmoji.C);
        super.addEmojiReaction(GameEmoji.D);
    }

    private String fix(String input) {
        return input.replace("&quot;", "\"").replace("&#039;", "'").replace("&amp;", "&");
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
        currentChoices[start] = fix(currentQuestionJSON.getString("correct_answer"));
        for (int i = 0; i < 3; i++) {
            start++;
            if (start == currentChoices.length) {
                start = 0;
            }
            currentChoices[start] = fix(currentQuestionJSON.getJSONArray("incorrect_answers").getString(i));
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
            win("🏆 " + getPThisTurn().getMention() + " wins!\n\n" + formatScores(getPThisTurn(), getPNextTurn()), getPThisTurn());
        } else if (scoresMap.get(getPThisTurn()).equals(scoresMap.get(getPNextTurn()))) {
            super.tie("Even scores! Tie!\n\n" + formatScores(getPNextTurn(), getPThisTurn()));
        } else {
            win("🏆 " + getPNextTurn().getMention() + " wins!\n\n" + formatScores(getPNextTurn(), getPThisTurn()), getPNextTurn());
        }
    }

    private String formatScores(Member winner, Member loser) {
        return "**" + scoresMap.get(winner) + "** ― " + winner.getMention()
                + "\n**" + scoresMap.get(loser) + "** ― " + loser.getMention();
    }

    @Override
    protected void onTurn(String input) {
        //if right, add point, next question
        if (getGuessFromEmoji(input).equals(fix(currentQuestionJSON.getString("correct_answer")))) {
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
        guessingPlayer = player;
        if (raw.equals(GameEmoji.EXIT)) {
            questionTimer.cancel();
            win(getForfeitMessage(player), getOtherPlayer(player));
        } else if (isValidInput(raw)) {
            onTurn(emoji.asUnicodeEmoji().map(ReactionEmoji.Unicode::getRaw).orElse(""));
        }
    }

    @Override
    protected boolean isValidInput(String input) {
        return !cooldown && getIndexFromLetterEmoji(input) >= 0 && getIndexFromLetterEmoji(input) < currentChoices.length
                && !guessedPlayers.contains(guessingPlayer.getId().asLong());
    }

    @Override
    protected String getBoard() {
        return "**" + fix(currentQuestionJSON.getString("question")) + "**\n"
                + "\n" + GameEmoji.A + " " + currentChoices[0]
                + "\n" + GameEmoji.B + " " + currentChoices[1]
                + "\n" + GameEmoji.C + " " + currentChoices[2]
                + "\n" + GameEmoji.D + " " + currentChoices[3];
    }

}
