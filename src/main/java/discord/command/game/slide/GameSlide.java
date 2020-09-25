package discord.command.game.slide;

import discord.core.game.TypeGame;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;

public class GameSlide extends TypeGame {

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
    private final Difficulty difficulty;

    public GameSlide(Message message, Member[] players, Difficulty difficulty) {
        super(message, players, 0);
        this.board = new TileBoard(difficulty);
        this.difficulty = difficulty;
    }

    @Override
    protected String getGameTitle() {
        return "Slide Puzzle";
    }

    @Override
    protected String getForfeitMessage(Member forfeiter) {
        return "You forfeited.";
    }

    @Override
    protected String getIdleMessage(Member idler) {
        return "You failed to move in time.";
    }

    @Override
    protected void onStart() {
        super.setInfoDisplay("Order the board from 1-8, moving one piece at time.\nUse **WASD** to move.");
    }

    @Override
    protected void onTurn(String input) {
        board.move(input);
        if (board.isSolved()) {
            super.win("**You win!**\n" + getBoard(), super.getPThisTurn(), difficulty.getWinAmount());
        } else {
            super.setInfoDisplay(""); //Allows a huge emoji message
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
