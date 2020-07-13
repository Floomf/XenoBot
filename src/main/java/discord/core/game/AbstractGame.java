package discord.core.game;

import discord.Main;
import discord.data.UserManager;
import discord.util.DiscordColor;
import discord4j.core.object.entity.Member;

import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import discord4j.core.object.entity.Message;

public abstract class AbstractGame {

    private final Message gameMessage;
    private final Member[] players;
    private int betAmount;

    private Timer idleTimer;

    private boolean active;
    private int turn = 0;
    private Member playerThisTurn;

    public AbstractGame(Message message, Member[] players, int betAmount) {
        this.gameMessage = message;
        this.players = players;
        this.betAmount = betAmount;
        this.idleTimer = new Timer();
        this.active = false;
    }

    abstract protected String getGameTitle();

    abstract protected String getForfeitMessage(Member forfeiter);

    abstract protected String getIdleMessage(Member idler);

    //For setting up specific game types (button manager and message listener);
    abstract protected void setup();

    //Games are responsible for calling updateMessageDisplay() on start
    abstract protected void onStart();

    abstract protected void onEnd();

    abstract protected String getBoard();

    public final void start() {
        if (!active) { //is this necessary?
            playerThisTurn = players[0];
            active = true;
            setup();
            startIdleTimer(playerThisTurn);
            onStart();
        }
    }

    protected final void win(String winMessage, Member winner, int winAmount) {
        //TODO i cant think correctly clean this later
        if (winAmount > 0) {
            if (betAmount == 0 && players.length == 1) { //blackjack has bets but is singleplayer
                long winnerID = winner.getId().asLong();
                if (GameManager.usersMoneyEarned.containsKey(winnerID)
                        && GameManager.usersMoneyEarned.get(winnerID) >= GameManager.EARN_LIMIT) {
                    winMessage += "\n\n💵 **No money earned.**\n*(Limit resets at 9AM daily)*";
                } else {
                    if (!GameManager.usersMoneyEarned.containsKey(winnerID)) {
                        GameManager.usersMoneyEarned.put(winnerID, 0);
                    }

                    if (GameManager.usersMoneyEarned.get(winnerID) + winAmount >= GameManager.EARN_LIMIT) { //shave off extra money to match the limit
                        winAmount = GameManager.EARN_LIMIT - GameManager.usersMoneyEarned.get(winnerID);
                        winMessage += "\n\n💵 **$" + winAmount + " earned.**\n(Earning limit reached)";
                    } else {
                        winMessage += "\n\n💵 **$" + winAmount + " earned.**";
                    }
                    GameManager.usersMoneyEarned.put(winnerID, GameManager.usersMoneyEarned.get(winnerID) + winAmount);
                    UserManager.getDUserFromMember(winner).addBalance(winAmount);
                }
            } else {
                UserManager.getDUserFromMember(winner).addBalance(winAmount);
                winMessage += "\n\n💵 **$" + winAmount + " won.**";
            }
        }

        //some games don't want getBoard() to display at end, so don't use setInfoDisplay()
        setGameDisplay(winMessage, DiscordColor.GREEN.getColor()); //discord online color
        end();
    }

    protected final void win(String winMessage, Member winner) {
        UserManager.getDUserFromUser(getOtherPlayer(winner)).addBalance(-betAmount); //will still fire if betAmount is 0
        win(winMessage, winner, betAmount);
    }

    //used for single player games
    protected final void win(String winMessage) {
        win(winMessage + "\n\n" + getBoard(), getPThisTurn(), betAmount);
    }

    //used for single player games
    protected final void lose(String loseMessage) {
        if (betAmount > 0) {
            UserManager.getDUserFromMember(getPThisTurn()).addBalance(-betAmount);
            loseMessage += "\n\n💵 **$" + betAmount + " lost.**";
        }
        setGameDisplay(loseMessage, DiscordColor.RED.getColor());
        end();
    }

    protected final void tie(String tieMessage) {
        //TODO bets are taken only when someone wins
        setGameDisplay(tieMessage + "\n\n" + getBoard(), DiscordColor.ORANGE.getColor());
        end();
    }

    private void end() {
        if (active) { //is this necessary?
            idleTimer.cancel();
            active = false;
            GameManager.removeGame(gameMessage);
            onEnd();
        }
    }

    protected void setBetAmount(int betAmount) {
        this.betAmount = betAmount;
    }

    private void setEntireMessage(String outside, String embedText, Color color) {
        gameMessage.edit(spec -> {
            spec.setContent(outside);
            spec.setEmbed(embed -> {
                embed.setDescription(embedText);
                embed.setAuthor(getGameTitle(), "", Main.BOT_AVATAR_URL);
                embed.setColor(color);
            });
        }).block();
    }

    private void setEntireMessage(String outside, String embedText) {
        setEntireMessage(outside, embedText, Color.WHITE);
    }

    protected final void setGameDisplay(String text) {
        setEntireMessage("", text);
    }

    protected final void setGameDisplay(String text, Color color) {
        setEntireMessage("", text, color);
    }

    protected final void setInfoDisplay(Member memberToPing, String info) {
        setEntireMessage(memberToPing.getMention(), info + "\n\n" + getBoard());
    }

    protected final void setInfoDisplay(String info) {
        setGameDisplay(info + "\n\n" + getBoard());
    }

    public final boolean isActive() {
        return active;
    }

    protected final Message getGameMessage() {
        return gameMessage;
    }

    protected final int getBetAmount() {
        return betAmount;
    }

    protected final Member getPNextTurn() {
        return players[(turn + 1) % players.length];
    }

    protected final Member getPThisTurn() {
        return playerThisTurn;
    }

    //Works with 1-2 player games
    protected final Member getOtherPlayer(Member player) {
        if (player.equals(playerThisTurn)) {
            return getPNextTurn();
        } else {
            return playerThisTurn;
        }
    }

    public final boolean playerIsInGame(Member player) {
        for (Member p : players) {
            if (p.equals(player)) return true;
        }
        return false;
    }

    protected final void setupNextTurn() {
        playerThisTurn = getPNextTurn();
        turn++;
        startIdleTimer(playerThisTurn);
    }

    private void startIdleTimer(Member player) {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (players.length == 1) {
                    lose(getIdleMessage(player));
                } else {
                    win(getIdleMessage(player), getOtherPlayer(player));
                }
            }
        };
        idleTimer.cancel();
        idleTimer = new Timer();
        idleTimer.schedule(task, TimeUnit.MINUTES.toMillis(10));
    }
    
}

