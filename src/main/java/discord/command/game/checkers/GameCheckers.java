package discord.command.game.checkers;

import discord.core.game.GameEmoji;
import discord.core.game.MultiplayerGame;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;

public class GameCheckers extends MultiplayerGame {

    enum Square {
        EMPTY(":black_large_square:", 0, false),
        UNPLAYABLE(":white_large_square:", 0, false),
        RED_MAN(":red_circle:", 1, false),
        BLUE_MAN(":blue_circle:", 2, false),
        RED_KING(":heart:", 1, true),
        BLUE_KING(":blue_heart:", 2, true);

        private final String emoji;
        private final int team;
        private final boolean isKing;

        Square(String emoji, int team, boolean isKing) {
            this.emoji = emoji;
            this.team = team;
            this.isKing = isKing;
        }

        Square king() { //TODO redesign?
            if (this == RED_MAN) {
                return RED_KING;
            } else {
                return BLUE_KING;
            }
        }

        boolean isOppositeTeam(Square other) {
            return team + other.team == 3;
        }
    }

    private static final int LENGTH = 8, HEIGHT = 8;

    private Message[] boardMessages;

    private final Square[][] board = new Square[HEIGHT][LENGTH];
    private Member player1;
    private Member player2;

    private int fromRow = 0;
    private int fromCol = 0;
    private int toRow = 7;
    private int toCol = 7;

    private String redCaptured = "";
    private String blueCaptured = "";

    private int turnsSinceCapture = 0;
    private boolean hasAnotherCapture = false;

    public GameCheckers(String gameTitle, TextChannel channel, Member[] players, int betAmount) {
        super(gameTitle, channel, players, betAmount);
    }

    @Override
    protected boolean useEmbed() {
        return false;
    }

    @Override
    protected String getForfeitMessage(Member forfeiter) {
        return getPiece(forfeiter) + " " + forfeiter.getMention() + " forfeited.\n"
                + getPiece(getOtherPlayer(forfeiter)) + " " + super.getOtherPlayer(forfeiter).getMention() + " wins!\n\n" + getBoard();
    }

    @Override
    protected String getIdleMessage(Member idler) {
        return idler.getMention() + " failed to go in time.\n" + super.getOtherPlayer(idler).getMention() + " wins!\n\n" + getBoard();
    }

    @Override
    protected void setup() {
        player1 = super.getPThisTurn();
        player2 = super.getPNextTurn();

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (i % 2 == 0 && j % 2 == 0 || i % 2 == 1 && j % 2 == 1) {
                    board[i][j] = Square.UNPLAYABLE;
                } else {
                    if (i < 3) {
                        board[i][j] = Square.BLUE_MAN;
                    } else if (i >= HEIGHT - 3) {
                        board[i][j] = Square.RED_MAN;
                    } else {
                        board[i][j] = Square.EMPTY;
                    }
                }
            }
        }
        boardMessages = new Message[] {getChannel().createMessage("Loading..").block(),
                getChannel().createMessage("Loading..").block(),
                getChannel().createMessage("Loading..").block()};

        updateBoard();
    }

    @Override
    protected String getFirstDisplay() {
        return "Enter your piece coord and the coord to move it to (Ex: **b3a4**)\n\n"
                + getPiece(player1) + " You start off, " + player1.getMention();
    }

    @Override
    protected void onStart() {
        super.registerMessageListener();
    }

    @Override //TODO clean up
    protected void onTurn(String input) {
        board[toRow][toCol] = board[fromRow][fromCol];
        board[fromRow][fromCol] = Square.EMPTY;

        turnsSinceCapture++;
        String info = "";
        boolean hasCaptured = false;

        if (Math.abs(fromRow - toRow) == 2 && Math.abs(fromCol - toCol) == 2) {
            addToCaptured(board[(fromRow + toRow) / 2][(fromCol + toCol) / 2]);
            board[(fromRow + toRow) / 2][(fromCol + toCol) / 2] = Square.EMPTY;
            info += ":comet: **Capture!**\n";
            hasCaptured = true;
            turnsSinceCapture = 0;
            hasAnotherCapture = false; //always set
        }

        //handle king
        if (!board[toRow][toCol].isKing && (toRow == 0 || toRow == HEIGHT - 1)) {
            board[toRow][toCol] = board[toRow][toCol].king();
            info += "👑 **King!**\n";
        }

        if (turnsSinceCapture == 30) {
            updateBoard();
            super.tie("**Stalemate.**");
            return;
        }

        if (playerHasNoPieces(super.getPNextTurn())) {
            updateBoard();
            super.win(getPiece(super.getPThisTurn()) + " " + super.getPThisTurn().getMention() + " wins!", super.getPThisTurn());
            return;
        }

        if (hasCaptured && pieceHasValidJump(toRow, toCol)) {
            hasAnotherCapture = true;
            info += "**Capture again " + getPThisTurn().getMention() + "!**\n";
            updateBoard();
            super.setupNextTurn(); //hack to skip other players turn
            super.setGameDisplay(getCapturedString() + info);
            return;
        }

        if (playerHasNoMoves(super.getPNextTurn())) {
            updateBoard();
            super.win(super.getPNextTurn().getMention() + " has no valid move.\n**" + super.getPThisTurn().getMention() + " wins!**", super.getPThisTurn());
            return;
        }

        updateBoard();
        super.setGameDisplay(getCapturedString() + info + "\n" + super.getPThisTurn().getDisplayName() + " moved **"
                + input.toUpperCase().substring(0, 2) + " > " + input.toUpperCase().substring(2, 4) + "**.\n" +
                getPiece(super.getPNextTurn()) + " Your turn, " + super.getPNextTurn().getMention());
    }

    private String getCapturedString() {
        if (redCaptured.isEmpty() && blueCaptured.isEmpty()) {
            return "";
        }
        return "Captured: " + redCaptured + "   " + blueCaptured + "\n";
    }

    private void addToCaptured(Square piece) {
        if (piece.team == 1) {
            redCaptured += piece.emoji;
        } else {
            blueCaptured += piece.emoji;
        }
    }

    @Override
    protected boolean isValidInput(String input) {
        input = input.replace(" ", "").replace(">", "");
        if (input.matches("([a-d][1-8]){2}")) {
            int fromR = HEIGHT - (input.charAt(1) - '0');
            int fromC = (input.charAt(0) - 'a') * 2 + ((fromR + 1) % 2); //its magic

            int toR = HEIGHT - (input.charAt(3) - '0');
            int toC = (input.charAt(2) - 'a') * 2 + ((toR + 1) % 2); //its magic

            if (isPieceForPlayer(getPThisTurn(), board[fromR][fromC]) && isValidMove(fromR, fromC, toR, toC) && (!hasAnotherCapture
                    || (fromR == toRow && fromC == toCol && Math.abs(fromR - toR) == 2 && Math.abs(fromC - toC) == 2))) { //must have another capture
                fromRow = fromR;
                fromCol = fromC;
                toRow = toR;
                toCol = toC;
                return true;
            } //else return isValidMove(selectedPieceRow, selectedPieceCol, row, col);
        }
        return false;
    }

    private boolean isPieceForPlayer(Member player, Square square) {
        return player.equals(player1) && (square == Square.RED_MAN || square == Square.RED_KING)
                || player.equals(player2) && (square == Square.BLUE_MAN || square == Square.BLUE_KING);
    }

    private boolean playerHasNoPieces(Member player) {
        int team = player == player1 ? 1 : 2;
        for (Square[] row : board) {
            for (Square col : row) {
                if (col.team == team) return false;
            }
        }
        return true;
    }

    private boolean playerHasNoMoves(Member player) {
        int team = player == player1 ? 1 : 2;
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j].team == team && pieceHasValidMove(i, j)) return false;
            }
        }
        return true;
    }

    private boolean isValidMove(int fromR, int fromC, int toR, int toC) {
        return toR > -1 && toR < HEIGHT && toC > -1 && toC < LENGTH //check for out of bounds
                && ((board[fromR][fromC].team == 1 && toR < fromR || board[fromR][fromC].team == 2 && toR > fromR) || board[fromR][fromC].isKing) //check for king to allow backwards
                && (board[toR][toC] == Square.EMPTY) //check for empty square
                && ((Math.abs(fromR - toR) == 1 && Math.abs(fromC - toC) == 1) //check if its a single move
                || board[(fromR + toR) / 2][(fromC + toC) / 2].isOppositeTeam(board[fromR][fromC])); //otherwise check for a jump
    }

    private boolean pieceHasValidNonJump(int row, int col) {
        Square piece = board[row][col];

        int dir = piece.team == 1 ? 1 : -1;

        if ((row > 0 && piece.team == 1 || row < HEIGHT - 1 && piece.team == 2) //check right direction
                && (col > 0 && board[row - dir][col - 1] == Square.EMPTY
                || col < LENGTH - 1 && board[row - dir][col + 1] == Square.EMPTY)) {
            return true;
        } else if (piece.isKing && (row > 0 && piece.team == 2 || row < HEIGHT - 1 && piece.team == 1) //check other direction
                && (col > 0 && board[row + dir][col - 1] == Square.EMPTY
                || col < LENGTH - 1 && board[row + dir][col + 1] == Square.EMPTY)) {
            return true;
        }
        return false;
    }

    private boolean pieceHasValidJump(int row, int col) {
        Square piece = board[row][col];

        int dir = piece.team == 1 ? 1 : -1;

        if ((row > 1 && piece.team == 1 || row < HEIGHT - 2 && piece.team == 2)  //check right direction
                && (col > 1 && board[row - dir][col - 1].isOppositeTeam(piece) && board[row - (2 * dir)][col - 2] == Square.EMPTY
                || col < LENGTH - 2 && board[row - dir][col + 1].isOppositeTeam(piece) && board[row - (2 * dir)][col + 2] == Square.EMPTY)) {
            return true;
        } else if (piece.isKing && (row > 1 && piece.team == 2 || row < HEIGHT - 2 && piece.team == 1) //check other direction (for king)
                && (col > 1 && board[row + dir][col - 1].isOppositeTeam(piece) && board[row + (2 * dir)][col - 2] == Square.EMPTY
                || col < LENGTH - 2 && board[row + dir][col + 1].isOppositeTeam(piece) && board[row + (2 * dir)][col + 2] == Square.EMPTY)) {
            return true;
        }

        return false;
    }

    private boolean pieceHasValidMove(int row, int col) {
        return pieceHasValidJump(row, col) || pieceHasValidNonJump(row, col);
    }

    private String getPiece(Member player) {
        if (player.equals(player1)) {
            return Square.RED_MAN.emoji;
        } else {
            return Square.BLUE_MAN.emoji;
        }
    }

    private void updateBoard() {
        StringBuilder sb = new StringBuilder();
        for (int i = Math.min(fromRow, toRow) / 3; i <= Math.max(fromRow, toRow) / 3; i++) {
            for (int j = i * 3; j < i * 3 + 3 && j < board.length ; j++) {
                sb.append(GameEmoji.intToNumberEmoji(HEIGHT - j));
                for (Square square : board[j]) {
                    sb.append(square.emoji).append(" ");
                }
                sb.append("\n");
            }
            if (i == 2) {
                sb.append(":record_button::regional_indicator_a: :regional_indicator_a: :regional_indicator_b: :regional_indicator_b:")
                        .append(" :regional_indicator_c: :regional_indicator_c: :regional_indicator_d: :regional_indicator_d:");
            }
            boardMessages[i].edit(spec -> spec.setContent(sb.toString())).block();
            sb.setLength(0);
        }
    }

    @Override
    protected String getBoard() {
        return "";
    }

}
