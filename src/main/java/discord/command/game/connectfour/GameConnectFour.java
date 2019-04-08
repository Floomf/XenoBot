package discord.command.game.connectfour;

import discord.core.game.Button;
import discord.core.game.ButtonGame;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class GameConnectFour extends ButtonGame {
       
    private static final int LENGTH = 7, HEIGHT = 6;    
    
    private static final int RED = 1, BLUE = -1, EMPTY = 0;
    
    private final int[][] board = new int[HEIGHT][LENGTH];
    private IUser redPlayer;
    private IUser bluePlayer;
    
    public GameConnectFour(IMessage message, IUser[] players) {
        super(message, players);
        super.getButtonManager().addNumButtons(message, LENGTH);
    }
    
    @Override
    protected void onStart() {
        redPlayer = super.getThisTurnUser();
        bluePlayer = super.getNextTurnUser();
        super.updateMessageDisplay(formatMessage(redPlayer, "You start off, " + redPlayer));
    }
    
    @Override
    protected void onTurn(int input) {
        placePiece(super.getThisTurnUser(), input - 1);
        if (playerHasWon(super.getThisTurnUser())) {
            IUser winner = super.getThisTurnUser();
            super.win(winner, formatMessage(winner, winner.getName() + " wins!"));
        } else if (boardIsFull()) {
            super.tie("Board is full. Tie!");
        } else {
            super.updateMessageDisplay(super.getThisTurnUser().getName() + " went in Slot ```" + input + "```\n" 
                    + formatMessage(super.getNextTurnUser(), "Your turn, " + super.getNextTurnUser()));
        }
    }       
    
    @Override
    protected boolean isValidInput(int input) {
        //if top slot of column empty, c is still open
        //remember arrays are 0 based
        return (board[0][input - 1] == EMPTY); 
    }
    
    private void placePiece(IUser player, int col) {
        for (int row = board.length - 1; row >= 0; row--) {
            if (board[row][col] == EMPTY) {
                board[row][col] = getPieceForPlayer(player);
                return;
            }
        }
    }
    
    private String formatMessage(IUser player, String message) {
        return getUnicodeForPiece(getPieceForPlayer(player)) + " " + message;
    }
       
    @Override
    protected String getBoard() {
        StringBuilder sb = new StringBuilder();
        
        //Append the row of numbers
        for (int i = 1; i <= 7; i++) {
            sb.append(Button.getFromNum(i).getEmoji());
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
    
    private boolean playerHasWon(IUser player) {
        int winSum = getPieceForPlayer(player) * 4;

        for (int r = HEIGHT - 1; r >= 0; r--) { //start from bottom row
            for (int c = 0; c < LENGTH; c++) {
                if (board[r][c] == EMPTY) continue;
                //Check up
                if (r - 3 >= 0) { //if theres 3 more slots up
                    if (board[r][c] + board[r - 1][c]  + board[r - 2][c] 
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
    
    private int getPieceForPlayer(IUser player) {
        if (player.equals(redPlayer)) {
            return RED;
        } else if (player.equals(bluePlayer)) {
            return BLUE;
        }
        return EMPTY;
    }
    
    private String getUnicodeForPiece(int piece) {
        switch (piece) {
            case RED:
                return "🔴"; //Red circle
            case BLUE:
                return "🔵"; //Blue Circle
            case EMPTY:
                return "⚫"; //Black circle
        }
        return "";
    }
}
