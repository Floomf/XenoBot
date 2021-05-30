package discord.command.game.slide;

import discord.core.game.GameEmoji;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

public class TileBoard {

    private enum Direction {
        UP, DOWN, LEFT, RIGHT;

        //When we move, we are actually moving the empty square around
        static Direction fromInput(String input) {
            if (input.equalsIgnoreCase("w")) {
                return DOWN;
            } else if (input.equalsIgnoreCase("s")) {
                return UP;
            } else if (input.equalsIgnoreCase("a")) {
                return RIGHT;
            } else if (input.equalsIgnoreCase("d")) {
                return LEFT;
            }
            return null;
        }

        static Direction getOpposite(Direction dir) {
            switch (dir) {
                case UP:
                    return DOWN;
                case DOWN:
                    return UP;
                case LEFT:
                    return RIGHT;
                case RIGHT:
                    return LEFT;
            }
            return null;
        }
    }

    private final int[] board = {1, 2, 3, 4, 5, 6, 7, 8, 0};
    private int index;

    public TileBoard(GameSlide.Difficulty difficulty) {
        index = board.length - 1;
        shuffle(difficulty.getNumMoves());
    }

    public TileBoard() {
        index = board.length - 1;
    }

    private String boardToString() {
        StringBuilder sb = new StringBuilder();
        for (int i : board) {
            sb.append(i);
        }
        return sb.toString();
    }

    protected void shuffle(int movesToSolve) {
        Random rand = new Random();
        HashSet<String> prevBoards = new HashSet<>(); //using strings cause unique
        Direction blockedMove = Direction.DOWN;
        for (int i = 0; i < movesToSolve; i++) {
            ArrayList<Direction> validMoves = new ArrayList<>();

            for (Direction dir : Direction.values()) {
                if (dir != blockedMove && isValidMove(dir)) {
                    move(dir);
                    if (!prevBoards.contains(boardToString())) {
                        validMoves.add(dir);
                    }
                    move(Direction.getOpposite(dir)); //move back
                }
            }

            Direction move = validMoves.get(rand.nextInt(validMoves.size()));
            move(move);
            prevBoards.add(boardToString());
            blockedMove = Direction.getOpposite(move);
        }
    }

    private void move(Direction dir) {
        if (dir == Direction.LEFT) {
            board[index] = board[index - 1];
            index--;
        } else if (dir == Direction.RIGHT) {
            board[index] = board[index + 1];
            index++;
        } else if (dir == Direction.UP) {
            board[index] = board[index - 3];
            index -= 3;
        } else if (dir == Direction.DOWN) {
            board[index] = board[index + 3];
            index += 3;
        }
        board[index] = 0;
    }

    public void move(String input) {
        move(Direction.fromInput(input));
    }

    private boolean isValidMove(Direction dir) {
        if (dir == null) {
            return false;
        } else if (dir == Direction.LEFT) {
            return index % 3 != 0;
        } else if (dir == Direction.RIGHT) {
            return (index - 2) % 3 != 0;
        } else if (dir == Direction.DOWN) {
            return index < 6;
        } else if (dir == Direction.UP) {
            return index > 2;
        }
        return false;
    }

    public boolean isValidMove(String input) {
        return isValidMove(Direction.fromInput(input));
    }

    public boolean isSolved() {
        for (int i = 0; i < board.length - 1; i++) {
            if (board[i] != i + 1) {
                return false;
            }
        }
        return true;
    }

    public String toString() {
        String boardDisplay = "";
        for (int i = 0; i < board.length; i++) {

            if (i % 3 == 0) {
                boardDisplay += "\n";
            }

            if (board[i] == 0) {
                boardDisplay += "⬛";
            } else {
                boardDisplay += GameEmoji.intToNumberEmoji(board[i]);
            }
        }
        return boardDisplay;
    }

}
