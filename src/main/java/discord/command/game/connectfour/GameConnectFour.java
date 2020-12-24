package discord.command.game.connectfour;

import discord.core.game.GameEmoji;
import discord.core.game.MultiplayerGame;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;

import java.util.Arrays;
import java.util.Random;

public class GameConnectFour extends MultiplayerGame {

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

    public GameConnectFour(String gameTitle, TextChannel channel, Member[] players, int betAmount) {
        super(gameTitle, channel, players, betAmount);
    }

    @Override
    protected boolean useEmbed() {
        return false;
    }

    @Override
    protected String getForfeitMessage(Member forfeiter) {
        return getPiece(forfeiter).emoji + " " + forfeiter.getMention() + " forfeited.\n"
                + getPiece(getOtherPlayer(forfeiter)).emoji + " " + super.getOtherPlayer(forfeiter).getMention() + " wins!\n\n" + getBoard();
    }

    @Override
    protected String getIdleMessage(Member idler) {
        return idler.getMention() + " failed to go in time.\n" + super.getOtherPlayer(idler).getMention() + " wins!\n\n" + getBoard();
    }

    @Override
    protected void setup() {
        player1 = super.getPThisTurn();
        for (Piece[] pieces : board) {
            Arrays.fill(pieces, Piece.EMPTY);
        }

        rowMessages[0] = getChannel().createMessage(":one: :two: :three: :four: :five: :six: :seven:"
                + "\n:white_circle: :white_circle: :white_circle: :white_circle: :white_circle: :white_circle: :white_circle:").block();
        for (int i = 1; i < rowMessages.length; i++) {
            rowMessages[i] = getChannel().createMessage(":white_circle: :white_circle: :white_circle: " +
                    ":white_circle: :white_circle: :white_circle: :white_circle:").block();
        }

        randomizePieceEmojis();
    }

    @Override
    protected String getFirstDisplay() {
        return getPiece(player1).emoji + " You start off, " + player1.getMention() + "\nClick an emoji to place your piece.";
    }

    @Override
    protected void onStart() {
        super.registerReactionListener();
        super.addEmojiReaction(GameEmoji.ONE);
        super.addEmojiReaction(GameEmoji.TWO);
        super.addEmojiReaction(GameEmoji.THREE);
        super.addEmojiReaction(GameEmoji.FOUR);
        super.addEmojiReaction(GameEmoji.FIVE);
        super.addEmojiReaction(GameEmoji.SIX);
        super.addEmojiReaction(GameEmoji.SEVEN);
        super.addEmojiReaction(GameEmoji.EXIT);
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
    protected void onTurn(String input) {
        dropPiece(getPiece(super.getPThisTurn()), GameEmoji.numberEmojiToInt(input) - 1);
        if (playerHasWon(super.getPThisTurn())) {
            Member winner = super.getPThisTurn();
            super.win(getPiece(super.getPThisTurn()).emoji + " " + winner.getMention() + " wins!", winner);
        } else if (boardIsFull()) {
            super.tie("Board is full. **Tie!**");
        } else {
            super.setGameDisplay(super.getPThisTurn().getDisplayName() + " went in slot **" + input + "**.\n"
                    + getPiece(super.getPNextTurn()).emoji + " Your turn, " + super.getPNextTurn().getMention());
        }
    }

    @Override
    protected boolean isValidInput(String input) {
        //if top slot of column empty, c is still open
        return GameEmoji.numberEmojiToInt(input) > 0 && board[0][GameEmoji.numberEmojiToInt(input) - 1] == Piece.EMPTY;
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
                String rowString = row == 0
                        ? ":one: :two: :three: :four: :five: :six: :seven:\n" + rowToString(board[row])
                        : rowToString(board[row]);;
                rowMessages[row].edit(spec -> spec.setContent(rowString)).block();
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

    @Override
    protected String getBoard() {
        return "";
    }
}

