package discord.command.game.math;

import discord.core.game.Leaderboard;
import discord.core.game.SingleplayerGame;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.TextChannel;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class GameMath extends SingleplayerGame {

    public static Leaderboard LEADERBOARD = new Leaderboard("leaderboard_math.json");

    private final Timer gameTimer = new Timer();
    private final Random rand = new Random();

    private final int[] elements = new int[2];
    private char operation;

    private int score = 0;

    public GameMath(String gameTitle, TextChannel channel, Member player, int betAmount) {
        super(gameTitle, channel, player, 0);
    }

    @Override
    protected void setup() {
        genNewProblem();
        gameTimer.schedule(new TimerTask() {
            public void run() {
                endGame();
            }
        }, TimeUnit.SECONDS.toMillis(60));
    }

    @Override
    protected String getFirstDisplay() {
        return "**Start!** Enter the answer:\n\n" + getBoard();
    }

    @Override
    protected String getForfeitMessage() {
        return "**You forfeited.** No score was recorded.";
    }

    @Override
    protected String getIdleMessage() {
        return ""; //This will never be called for this game
    }

    @Override
    protected void onStart() {
        super.registerMessageListener(5);
    }

    @Override
    protected void onTurn(String input) {
        int answer = Integer.parseInt(input);

        if (isCorrect(answer)) {
            score++;
            genNewProblem();
            super.setInfoDisplay("✅ **Correct!**");
        } else {
            score--;
            genNewProblem();
            super.setInfoDisplay("❌ **Incorrect!**");
        }
    }

    @Override
    protected void onEnd() {
        gameTimer.cancel();
    }

    private synchronized void endGame() {
        String endMessage = "🛑 *Time's up!*\nYour score: **" + score + "**\n";

        if (LEADERBOARD.submitScore(getPlayer(), score)) { //true if new high score, bad cause side effect?
            endMessage += "\n**NEW HIGH SCORE!**\n\n";
        }

        endMessage += "Your best: **" + LEADERBOARD.highScoresMap.get(getPlayer().getId().asLong()) + "**";
        super.deletePlayerMessages();
        super.win(endMessage, 6 * (score + score / 5));
    }

    @Override
    protected boolean isValidInput(String input) {
        return input.matches("-?\\d+");
    }

    @Override
    protected String getBoard() {
        return "```" + elements[0] + " " + operation + " " + elements[1] + "```\n\nScore: " + score;
    }

    private boolean isCorrect(int answer) {
        if (operation == '+') {
            return answer == elements[0] + elements[1];
        } else {
            return answer == elements[0] - elements[1];
        }
    }

    private void genNewProblem() {
        elements[0] = rand.nextInt(76) + 15;
        elements[1] = rand.nextInt(76) + 15;
        if (rand.nextInt(2) == 0) {
            operation = '+';
        } else {
            operation = '-';
        }
    }

}
