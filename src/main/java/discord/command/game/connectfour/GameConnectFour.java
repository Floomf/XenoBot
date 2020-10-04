package discord.command.game.connectfour;

import discord.core.game.TypeGame;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;

import java.util.Arrays;
import java.util.Random;

public class GameConnectFour extends TypeGame {

    enum Piece {
        RED(1, ":red_circle:"), BLUE(-1, ":blue_circle:"), EMPTY(0, ":white_circle:");

        final int val;
        String emoji;

        Piece(int val, String emoji) {
            this.val = val;
            this.emoji = emoji;
        }

        void setEmoji(String emoji) {
            this.emoji = emoji;
        }
    }

    private static final int LENGTH = 7, HEIGHT = 6;

    private static final String[] CIRCLE_EMOJIS = {":red_circle:", ":blue_circle:",
            ":green_circle:", ":purple_circle:", ":yellow_circle:"};

    private final Message[] rowMessages = new Message[HEIGHT];
    private final Piece[][] board = new Piece[HEIGHT][LENGTH];
    private Member player1;

    public GameConnectFour(Message message, Member[] players, int betAmount) {
        super(message, players, betAmount);

        for (Piece[] pieces : board) {
            Arrays.fill(pieces, Piece.EMPTY);
        }

        message.edit(spec -> {
            spec.setContent(":one: :two: :three: :four: :five: :six: :seven:");
            spec.setEmbed(null);
        }).block();

        MessageChannel channel = message.getChannel().block();

        for (int i = 0; i < rowMessages.length; i++) {
            rowMessages[i] = channel.createMessage(":white_circle: :white_circle: :white_circle: " +
                    ":white_circle: :white_circle: :white_circle: :white_circle:").block();
        }

        super.setGameMessage(channel.createMessage("Loading...").block());

        randomizePieceEmojis();
    }

    @Override
    protected String getGameTitle() {
        return "Connect Four";
    }

    @Override
    protected String getForfeitMessage(Member forfeiter) {
        return forfeiter.getMention() + " forfeited.\n" + super.getOtherPlayer(forfeiter).getMention() + " wins!\n\n" + getBoard();
    }

    @Override
    protected String getIdleMessage(Member idler) {
        return idler.getMention() + " failed to go in time.\n" + super.getOtherPlayer(idler).getMention() + " wins!\n\n" + getBoard();
    }

    private void randomizePieceEmojis() {
        Random rand = new Random();
        int one = rand.nextInt(CIRCLE_EMOJIS.length);
        int two = rand.nextInt(CIRCLE_EMOJIS.length);
        while (two == one) {
            two = rand.nextInt(CIRCLE_EMOJIS.length);
        }
        Piece.RED.setEmoji(CIRCLE_EMOJIS[one]);
        Piece.BLUE.setEmoji(CIRCLE_EMOJIS[two]);
    }

    @Override
    protected void onStart() {
        player1 = super.getPThisTurn();
        super.setGameDisplay("Type a column **[1-7]** to place your piece.\n" + getPiece(player1).emoji + " You start off, " + player1.getMention());
    }

    @Override
    protected void onTurn(String input) {
        dropPiece(getPiece(super.getPThisTurn()), Integer.parseInt(input) - 1);
        if (playerHasWon(super.getPThisTurn())) {
            Member winner = super.getPThisTurn();
            super.win(getPiece(super.getPThisTurn()).emoji + " " + winner.getMention() + " wins!", winner);
        } else if (boardIsFull()) {
            super.tie("Board is full. **Tie!**");
        } else {
            super.setGameDisplay(super.getPThisTurn().getDisplayName() + " went in slot **" + input + "**.\n"
                    + getPiece(super.getPNextTurn()).emoji + " " + super.getPNextTurn().getMention());
        }
    }

    @Override
    protected boolean isValidInput(String input) {
        //if top slot of column empty, c is still open
        return input.matches("[1-7]") && board[0][Integer.parseInt(input) - 1] == Piece.EMPTY;
    }

    private Piece getPiece(Member player) {
        if (player.equals(player1)) {
            return Piece.RED;
        } else {
            return Piece.BLUE;
        }
    }

    private void dropPiece(Piece piece, int col) {
        for (int row = board.length - 1; row >= 0; row--) {
            if (board[row][col] == Piece.EMPTY) {
                board[row][col] = piece;
                final int temp = row;
                rowMessages[row].edit(spec -> spec.setContent(rowToString(board[temp]))).block();
                return;
            }
        }
    }

    private String rowToString(Piece[] row) {
        String rowString = "";
        for (Piece index : row) {
            rowString += index.emoji + " ";
        }
        return rowString;
    }

    private boolean boardIsFull() {
        for (Piece piece : board[0]) { //only have to check top row
            if (piece == Piece.EMPTY) return false;
        }
        return true;
    }

    private boolean playerHasWon(Member player) {
        int winSum = getPiece(player).val * 4;

        for (int r = HEIGHT - 1; r >= 0; r--) { //start from bottom row
            for (int c = 0; c < LENGTH; c++) {
                if (board[r][c] == Piece.EMPTY) continue;
                //Check up
                if (r - 3 >= 0) { //if theres 3 more slots up
                    if (board[r][c].val + board[r - 1][c].val + board[r - 2][c].val + board[r - 3][c].val == winSum) {
                        return true;
                    }
                }
                if (c + 3 < LENGTH) { //if theres 3 more slots to right
                    //Check right
                    if (board[r][c].val + board[r][c + 1].val + board[r][c + 2].val + board[r][c + 3].val == winSum) {
                        return true;
                    }
                    //Check diag up right
                    if (r - 3 >= 0) {
                        if (board[r][c].val + board[r - 1][c + 1].val + board[r - 2][c + 2].val + board[r - 3][c + 3].val == winSum) {
                            return true;
                        }
                    }
                    //Check diag down right
                    if (r + 3 < HEIGHT) {
                        if (board[r][c].val + board[r + 1][c + 1].val + board[r + 2][c + 2].val + board[r + 3][c + 3].val == winSum) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    //TODO redesign this
    @Override
    protected String getBoard() {
        return "";
    }
}

