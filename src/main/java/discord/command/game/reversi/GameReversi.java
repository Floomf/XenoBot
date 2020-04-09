package discord.command.game.reversi;

import discord.core.game.Button;
import discord.core.game.TypeGame;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;

public class GameReversi extends TypeGame {

    private static final int HEIGHT = 8, LENGTH = 8;

    private static final int RED = 1, BLUE = -1, EMPTY = 0, VALID = 2;

    private static final int NORTH = -1, SOUTH = 1;
    private static final int EAST = 1, WEST = -1;

    private final int[][] board = new int[HEIGHT][LENGTH];
    private Member redPlayer;
    private Member bluePlayer;

    public GameReversi(Message message, Member[] players) {
        super(message, players);
    }

    @Override
    protected String getGameTitle() {
        return "Reversi";
    }

    protected String getForfeitMessage(Member forfeiter) {
        return forfeiter.getMention() + " forfeits. " + super.getOtherUser(forfeiter).getMention() + " wins!\n\n" + getBoard();
    }

    @Override
    protected void onStart() {
        //Starting pieces
        board[3][3] = BLUE;
        board[3][4] = RED;
        board[4][3] = RED;
        board[4][4] = BLUE;
        redPlayer = super.getPlayerThisTurn();
        bluePlayer = super.getPlayerNextTurn();
        updateValidChoices(RED);
        super.setInfoDisplay(formatMessage(redPlayer, "You start off, " + redPlayer.getMention()
                + "\nEnter a grid position to place your piece (like \"f4\"):"));
    }

    @Override
    protected void onTurn(String input) {
        placePiece(getPieceForPlayer(super.getPlayerThisTurn()),
                getRowForGridNumber(input.charAt(1)), getColForGridLetter(input.charAt(0)));

        if (updateValidChoices(getPieceForPlayer(super.getPlayerNextTurn()))) {
            super.setInfoDisplay(super.getPlayerThisTurn().getDisplayName() + " went at `" + input.toUpperCase() + "`\n"
                    + formatMessage(super.getPlayerNextTurn(), "Your turn, " + super.getPlayerNextTurn().getMention()));
        } else { //One player couldn't go
            super.setupNextTurn(); //Skip over their turn
            if (updateValidChoices(getPieceForPlayer(super.getPlayerNextTurn()))) {
                super.setInfoDisplay(super.getPlayerThisTurn().getDisplayName() + " went at `" + input.toUpperCase() + "`\n"
                        + formatMessage(super.getPlayerNextTurn(), super.getPlayerThisTurn().getDisplayName()
                        + " doesn't have a valid move. Your turn again, " + super.getPlayerNextTurn().getMention()));
            } else { //Both players couldn't go, so game is over
                findWinner();
            }
        }
    }

    @Override
    protected boolean isValidInput(String input) {
        if (input.matches("[a-h]\\d")) {  //regex for valid position on our grid
            int row = getRowForGridNumber(input.charAt(1));
            int col = getColForGridLetter(input.charAt(0));
            return (row > -1 && row < HEIGHT && col > -1 && col < LENGTH && positionIsValidAt(row, col));
        }
        return false;
    }

    private void findWinner() {
        int sum = 0;
        for (int row = 0; row < HEIGHT; row++) {
            for (int col = 0; col < LENGTH; col++) {
                sum += board[row][col];
            }
        }
        if (sum > 0) {
            super.win(formatMessage(redPlayer, redPlayer.getDisplayName() + " wins by " + sum + " pieces!") + "\n\n" + getBoard());
        } else if (sum < 0) {
            super.win(formatMessage(bluePlayer, bluePlayer.getDisplayName() + " wins by " + (-sum) + " pieces!") + "\n\n" + getBoard());
        } else {
            super.tie("Even split of captures! The game is a tie.");
        }
    }

    private void placePiece(int piece, int row, int col) {
        board[row][col] = piece;

        checkPieceLine(piece, row - 1, col, 0, NORTH, true); //Up
        checkPieceLine(piece, row + 1, col, 0, SOUTH, true); //Down
        checkPieceLine(piece, row, col + 1, EAST, 0, true); //Right
        checkPieceLine(piece, row, col - 1, WEST, 0, true); //Left

        checkPieceLine(piece, row - 1, col + 1, EAST, NORTH, true); //Up Right
        checkPieceLine(piece, row - 1, col - 1, WEST, NORTH, true); //Up Left
        checkPieceLine(piece, row + 1, col + 1, EAST, SOUTH, true); //Up Right
        checkPieceLine(piece, row + 1, col - 1, WEST, SOUTH, true); //Up Right
    }

    private boolean updateValidChoices(int piece) {
        boolean hasValidChoice = false;
        for (int row = 0; row < HEIGHT; row++) {
            for (int col = 0; col < LENGTH; col++) {
                if (board[row][col] == VALID) {
                    board[row][col] = EMPTY;
                }

                if (board[row][col] == EMPTY) {
                    if ((posIsPiece(-piece, row - 1, col) && checkPieceLine(piece, row - 2, col, 0, NORTH, false))
                            || (posIsPiece(-piece, row + 1, col) && checkPieceLine(piece, row + 2, col, 0, SOUTH, false))
                            || (posIsPiece(-piece, row, col + 1) && checkPieceLine(piece, row, col + 2, EAST, 0, false))
                            || (posIsPiece(-piece, row, col - 1) && checkPieceLine(piece, row, col - 2, WEST, 0, false))
                            || (posIsPiece(-piece, row - 1, col + 1) && checkPieceLine(piece, row - 2, col + 2, EAST, NORTH, false))
                            || (posIsPiece(-piece, row - 1, col - 1) && checkPieceLine(piece, row - 2, col - 2, WEST, NORTH, false))
                            || (posIsPiece(-piece, row + 1, col + 1) && checkPieceLine(piece, row + 2, col + 2, EAST, SOUTH, false))
                            || (posIsPiece(-piece, row + 1, col - 1) && checkPieceLine(piece, row + 2, col - 2, WEST, SOUTH, false))) {

                        board[row][col] = VALID;
                        hasValidChoice = true;
                    }
                }
            }
        }
        return hasValidChoice;
    }

    private String formatMessage(Member player, String message) {
        return getUnicodeForPiece(getPieceForPlayer(player)) + " " + message;
    }

    private boolean posIsPiece(int piece, int row, int col) {
        if ((row > HEIGHT - 1) || (row < 0) || (col > LENGTH - 1) || (col < 0)) { //out of bounds check
            return false;
        }
        return (board[row][col] == piece);
    }

    private boolean checkPieceLine(int piece, int row, int col, int xChange, int yChange, boolean flip) {
        if ((row > HEIGHT - 1) || (row < 0) || (col > LENGTH - 1) || (col < 0)) { //out of bounds check
            return false;
        }

        if (board[row][col] == piece) { //found our piece 
            return true;
        } else if (board[row][col] == -piece) { //opposite piece
            //keep going through our line and see if we find our piece
            if (checkPieceLine(piece, row + yChange, col + xChange, xChange, yChange, flip)) {
                if (flip) {
                    board[row][col] = piece; //we found our piece, so flip the rest in the line
                }
                return true;
            }
        }
        return false;
    }

    @Override
    protected String getBoard() {
        StringBuilder sb = new StringBuilder();

        for (int row = 0; row < HEIGHT; row++) {
            sb.append(Button.getFromNum(HEIGHT - row).getEmoji().asUnicodeEmoji().get().getRaw());
            for (int col = 0; col < LENGTH; col++) {
                sb.append(getUnicodeForPiece(board[row][col]));
            }
            sb.append("\n");
        }
        sb.append("⏺🇦\u200B🇧\u200B🇨\u200B🇩\u200B🇪\u200B🇫\u200B🇬\u200B🇭"); //Theres some zero width spaces in here to prevent flag emojis

        return sb.toString();
    }

    private int getPieceForPlayer(Member player) {
        if (player.equals(redPlayer)) {
            return RED;
        } else {
            return BLUE;
        }
    }

    private String getUnicodeForPiece(int piece) {
        switch (piece) {
            case EMPTY:
                return "⬛";
            case RED:
                return "🔴";
            case BLUE:
                return "🔵";
            case VALID:
                return "🔲";
        }
        return "";
    }


    private int getRowForGridNumber(char c) {
        //this works by subtracting ascii code for 0, learned this in nand2tetris too
        return HEIGHT - (c - '0');
    }

    private int getColForGridLetter(char c) {
        //same as previous method, its the fastest way to calculate these
        return (c - 'a');
    }

    private boolean positionIsValidAt(int row, int col) {
        return (board[row][col] == VALID);
    }

}

