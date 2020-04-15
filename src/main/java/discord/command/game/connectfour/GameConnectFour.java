package discord.command.game.connectfour;

import discord4j.core.object.entity.Message;
import discord.core.game.Button;
import discord.core.game.ButtonGame;
import discord4j.core.object.entity.Member;

import java.util.Random;

public class GameConnectFour extends ButtonGame {

    private static final int LENGTH = 7, HEIGHT = 6;

    private static final int RED = 1, BLUE = -1, EMPTY = 0;

    private static final String[] PIECES = {":red_circle:", ":blue_circle:",
            ":green_circle:", ":purple_circle:", ":yellow_circle:"};

    private final int[][] board = new int[HEIGHT][LENGTH];
    private Member player1;
    private Member player2;

    private String player1piece;
    private String player2piece;

    public GameConnectFour(Message message, Member[] players) {
        super(message, players);
        super.getButtonManager().addNumButtons(message, LENGTH);
        assignPieces();
    }

    @Override
    protected String getGameTitle() {
        return "Connect Four";
    }

    @Override
    protected String getForfeitMessage(Member forfeiter) {
        return forfeiter.getMention() + " forfeited. " + super.getOtherUser(forfeiter).getMention() + " wins!\n\n" + getBoard();
    }

    @Override
    protected String getIdleMessage(Member idler) {
        return idler.getMention() + " failed to go in time. " + super.getOtherUser(idler).getMention() + " wins!\n\n" + getBoard();
    }

    private void assignPieces() {
        Random rand = new Random();
        int one = rand.nextInt(PIECES.length);
        int two = rand.nextInt(PIECES.length);
        while (two == one) {
            two = rand.nextInt(PIECES.length);
        }
        player1piece = PIECES[one];
        player2piece = PIECES[two];
    }

    @Override
    protected void onStart() {
        player1 = super.getPlayerThisTurn();
        player2 = super.getPlayerNextTurn();
        super.setInfoDisplay(formatMessage(player1, "You start off, " + player1.getMention()));
    }

    @Override
    protected void onTurn(int input) {
        placePiece(super.getPlayerThisTurn(), input - 1);
        if (playerHasWon(super.getPlayerThisTurn())) {
            Member winner = super.getPlayerThisTurn();
            super.win(formatMessage(winner, winner.getMention() + " wins!\n\n" + getBoard()));
        } else if (boardIsFull()) {
            super.tie("Board is full. Tie!");
        } else {
            super.setInfoDisplay(super.getPlayerThisTurn().getMention() + " went in slot `" + input + "`\n"
                    + formatMessage(super.getPlayerNextTurn(), "Your turn, " + super.getPlayerNextTurn().getMention()));
        }
    }

    @Override
    protected boolean isValidInput(int input) {
        //if top slot of column empty, c is still open
        //remember arrays are 0 based
        return (board[0][input - 1] == EMPTY);
    }

    private void placePiece(Member player, int col) {
        for (int row = board.length - 1; row >= 0; row--) {
            if (board[row][col] == EMPTY) {
                board[row][col] = getPieceForPlayer(player);
                return;
            }
        }
    }

    private String formatMessage(Member player, String message) {
        return getUnicodeForPiece(getPieceForPlayer(player)) + " " + message;
    }


    @Override
    protected String getBoard() {
        StringBuilder sb = new StringBuilder();

        //Append the row of numbers
        for (int i = 1; i <= 7; i++) {
            sb.append(Button.getFromNum(i).getEmoji().asUnicodeEmoji().get().getRaw());
        }

        //Append the actual board
        for (int row = 0; row < board.length; row++) {
            sb.append("\n");
            for (int col = 0; col < board[0].length; col++) {
                sb.append(getUnicodeForPiece(board[row][col]));
            }
        }
        return sb.toString();
    }

    private boolean playerHasWon(Member player) {
        int winSum = getPieceForPlayer(player) * 4;

        for (int r = HEIGHT - 1; r >= 0; r--) { //start from bottom row
            for (int c = 0; c < LENGTH; c++) {
                if (board[r][c] == EMPTY) continue;
                //Check up
                if (r - 3 >= 0) { //if theres 3 more slots up
                    if (board[r][c] + board[r - 1][c] + board[r - 2][c]
                            + board[r - 3][c] == winSum) {
                        return true;
                    }
                }
                if (c + 3 < LENGTH) { //if theres 3 more slots to right
                    //Check right
                    if (board[r][c] + board[r][c + 1] + board[r][c + 2]
                            + board[r][c + 3] == winSum) {
                        return true;
                    }
                    //Check diag up right
                    if (r - 3 >= 0) {
                        if (board[r][c] + board[r - 1][c + 1] + board[r - 2][c + 2]
                                + board[r - 3][c + 3] == winSum) {
                            return true;
                        }
                    }
                    //Check diag down right
                    if (r + 3 < HEIGHT) {
                        if (board[r][c] + board[r + 1][c + 1] + board[r + 2][c + 2]
                                + board[r + 3][c + 3] == winSum) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean boardIsFull() {
        for (int piece : board[0]) { //only have to check top row
            if (piece == EMPTY) return false;
        }
        return true;
    }

    private int getPieceForPlayer(Member player) {
        if (player.equals(player1)) {
            return RED;
        } else if (player.equals(player2)) {
            return BLUE;
        }
        return EMPTY;
    }

    private String getUnicodeForPiece(int piece) {
        switch (piece) {
            case RED:
                return player1piece;
            case BLUE:
                return player2piece;
            case EMPTY:
                return "⚪"; //White circle
        }
        return "";
    }
}

