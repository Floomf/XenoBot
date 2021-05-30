package discord.command.game.slide;

import discord.core.game.SingleplayerGame;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.TextChannel;

import java.util.Random;

public class GameSlide extends SingleplayerGame {

    enum Difficulty {
        EASY(10, 20),  MEDIUM(15, 50), HARD(20, 150);

        private final int numMoves;
        private final int winAmount;

        Difficulty(int numMoves, int cashReward) {
            this.numMoves = numMoves;
            this.winAmount = cashReward;
        }

        public int getNumMoves() {
            return numMoves;
        }

        public int getWinAmount() {
            return winAmount;
        }

        public static Difficulty fromString(String string) {
            if (string.equalsIgnoreCase("easy")) {
                return EASY;
            } else if (string.equalsIgnoreCase("medium")) {
                return MEDIUM;
            } else if (string.equalsIgnoreCase("hard")) {
                return HARD;
            }
            return null;
        }
    }

    private final TileBoard board;
    //private final Difficulty difficulty;

    public GameSlide(String gameTitle, TextChannel channel, Member player, int betAmount) {
        super(gameTitle, channel, player, betAmount);
        this.board = new TileBoard();
        //this.difficulty = difficulty;
    }

    @Override
    protected boolean useEmbed() {
        return false;
    }

    @Override
    protected String getForfeitMessage() {
        return "You forfeited.";
    }

    @Override
    protected String getIdleMessage() {
        return "You failed to move in time.";
    }

    @Override
    protected void setup() {
        board.shuffle(new Random().nextInt(11) + 10); //10-20
    }

    protected String getFirstDisplay() {
        return "Rearrange the board from 1-8, moving one square at time.\nType **W, A, S, or D** to move.\n\n" + getBoard();
    }

    @Override
    protected void onStart() {
        super.registerMessageListener(5);
    }

    @Override
    protected void onTurn(String input) {
        board.move(input);
        if (board.isSolved()) {
            super.win("**You win!**\n" + getBoard(), 75);
        } else {
            super.setInfoDisplay("");
        }
    }

    @Override
    protected boolean isValidInput(String input) {
        return board.isValidMove(input);
    }

    @Override
    protected String getBoard() {
        return board.toString();
    }
}
