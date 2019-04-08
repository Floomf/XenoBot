package discord.core.game;

import discord.util.BotUtils;
import java.util.Timer;
import java.util.TimerTask;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public abstract class AbstractGame {
   
    private final IMessage gameMessage;
    
    private final IUser[] players;
    private Timer turnTimer;
    
    private boolean active;
    private int turn = 0;
    private IUser userThisTurn;
    
    public AbstractGame(IMessage message, IUser[] players) {
        this.gameMessage = message;
        this.players = players;
        this.turnTimer = new Timer();
        this.active = false;
    }
    
    //For setting up specific game types (button manager and message listener);
    abstract protected void setup();
    
    //Games are responsible for calling updateMessageDisplay() on start
    abstract protected void onStart();
    
    abstract protected void onEnd();
   
    abstract protected String getBoard();   
           
    public final void start() {
        if (!active) {
            userThisTurn = players[0];          
            active = true;
            startTurnTimer(userThisTurn);
            setup();
            onStart();
        }
    }
    
    protected final void win(IUser winner, String winMessage) {
        updateMessageDisplay(winMessage);
        end();
    }
    
    protected final void tie(String tieMessage) {
        updateMessageDisplay(tieMessage);
        end();
    }
     
    private void end() {
        if (active) {
            turnTimer.cancel();
            active = false;
            onEnd();
            GameManager.removeGame(gameMessage);
        }
    }
    
    public final boolean isActive() {
        return active;
    }
    
    protected final void updateMessageDisplay(String info) {
        BotUtils.editMessage(gameMessage, info + "\n" + getBoard());
    }   
    
    protected final IMessage getGameMessage() {
        return gameMessage;
    }
    
    protected final IUser getNextTurnUser() {
        return players[(turn + 1) % players.length];
    } 
    
    protected final IUser getThisTurnUser() {
        return userThisTurn;
    }
    
    //Only works with 2 player games
    protected final IUser getOtherUser(IUser player) {
        if (player == players[0]) {
            return players[1];
        } else {
            return players[0];
        }
    }
          
    protected final void setupNextTurn() {
        userThisTurn = getNextTurnUser();
        turn++;
        startTurnTimer(userThisTurn);
    }
   
    private void startTurnTimer(IUser player) {
        TimerTask task = new TimerTask() {        
            @Override
            public void run() {
                win(getNextTurnUser(), player.getName() + " failed to go in time. "
                        + getNextTurnUser().getName() + " wins!");
            }            
        };
        turnTimer.cancel();
        turnTimer = new Timer();
        turnTimer.schedule(task, 600000L); //10 minutes
    }
    
}
