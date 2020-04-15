package discord.command.game.math;

import com.fasterxml.jackson.databind.ObjectMapper;
import discord.core.game.Highscore;
import discord.core.game.TypeGame;
import discord.data.UserManager;
import discord.data.object.user.DUser;
import discord.util.ProfileBuilder;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.util.Snowflake;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class GameMath extends TypeGame {

    private Timer gameTimer = new Timer();
    private Random rand = new Random();

    private int[] elements = new int[2];
    private char operation;

    private int score = 0;

    //tried using TreeMap but it sorts the keys not the values
    private static HashMap<Snowflake, Highscore> highScores = new HashMap<>();

    static {
        try {
            Highscore[] loadScores = new ObjectMapper().readValue(new File("high_scores_math.json"), Highscore[].class);
            Arrays.stream(loadScores).forEach(hs -> highScores.put(Snowflake.of(hs.getDiscordID()), hs));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public GameMath(Message message, Member[] players) {
        super(message, players);
    }

    @Override
    protected String getGameTitle() {
        return "Quick Math";
    }

    @Override
    protected String getForfeitMessage(Member forfeiter) {
        gameTimer.cancel(); //cant be good design
        return "**You forfeited.** No score was recorded.";
    }

    @Override
    protected String getIdleMessage(Member idler) {
        return ""; //This will never be called for this game
    }

    @Override
    protected void onStart() {
        genNewProblem();
        super.setInfoDisplay("**Start!** Enter the answer:");

        gameTimer.schedule(new TimerTask() {
            public void run() {
                endGame();
                gameTimer.cancel();
            }
        }, TimeUnit.SECONDS.toMillis(60));
    }

    @Override
    protected void onTurn(String input) {
        if (super.isActive()) { //Another messy check because i dont know threads
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
    }

    private void endGame() {
        String endMessage = "🛑 **Time's up!**\nYour score: `" + score + "`\n";

        if (!highScores.containsKey(super.getPlayerThisTurn().getId())) {
            highScores.put(super.getPlayerThisTurn().getId(), new Highscore(super.getPlayerThisTurn().getId().asLong(), score));
            endMessage += "\n**NEW HIGH SCORE!**\n\n";
            saveScores();
        } else if (highScores.get(super.getPlayerThisTurn().getId()).validateNewScore(score)) {
            endMessage += "\n**NEW HIGH SCORE!**\n\n";
            saveScores();
        }

        endMessage += "Your best: `" + highScores.get(getPlayerThisTurn().getId()).getHighscore() + "`\n\n";

        DUser user = UserManager.getDUserFromMember(super.getPlayerThisTurn());
        if (user.getProg().isNotMaxLevel() && score > 0) {
            user.getProg().addXP(score);
            endMessage += "`+"  + score + " XP`\n\n";
        }

        endMessage += getHighscores();
        super.win(endMessage);
    }

    @Override
    protected boolean isValidInput(String input) {
        return input.matches("-?\\d+");
    }

    @Override
    protected String getBoard() {
        return "`" + elements[0] + " " + operation + " " + elements[1] + "`\n\nScore: " + score;
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

    private String getHighscores() {
        StringBuilder sb = new StringBuilder();
        Highscore[] sortedScores = highScores.values().stream()
                .sorted((score, score2) -> score2.getHighscore() - score.getHighscore()).toArray(Highscore[]::new);
        sb.append("__**High Scores**__ 🏆");

        int currPlace = 1;
        int lastScore = -1;
        for (Highscore score : sortedScores) {
            if (currPlace > 10) { //Only show top 10
                break;
            }

            if (score.getHighscore() == lastScore) {
                sb.append(", <@!").append(score.getDiscordID()).append(">");
            } else {
                sb.append("\n").append(getFormattedPlace(currPlace))
                        .append(" `").append(score.getHighscore())
                        .append("` - <@!").append(score.getDiscordID()).append(">");
                currPlace++;
            }
            lastScore = score.getHighscore();
        }
        return sb.toString();
    }

    private static String getFormattedPlace(int place) {
        if (place == 1) return "🥇   ";
        else if (place == 2) return "🥈   ";
        else if (place == 3) return "🥉   ";
        else return "**" + ProfileBuilder.getOrdinal(place) + ")**";
    }

    private void saveScores() {
        try {
            new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(new File("high_scores_math.json"), highScores.values());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
