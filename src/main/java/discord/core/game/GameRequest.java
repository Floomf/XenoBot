package discord.core.game;

import discord.util.BotUtils;
import java.lang.reflect.InvocationTargetException;
import java.util.Timer;
import java.util.TimerTask;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IReaction;
import sx.blah.discord.handle.obj.IUser;

public class GameRequest {
    
    private final String gameName;
    private final Class<? extends AbstractGame> gameType;
    private final IMessage requestMessage;
    private final IUser[] players;
    private final ButtonManager bm;
    private final Timer inactiveTimer;
    
    public GameRequest(String gameName, Class<? extends AbstractGame> gameType, 
            IMessage message, IUser[] players) {
        this.gameName = gameName;
        this.gameType = gameType;
        this.requestMessage = message;
        this.players = players;
        this.bm = new ButtonManager();
        this.inactiveTimer = new Timer();
        setup();
    }
    
    private void setup() {
        bm.addButton(requestMessage, Button.CHECKMARK);
        bm.addButton(requestMessage, Button.EXIT);
        BotUtils.editMessage(requestMessage, players[1].mention(), BotUtils.getBuilder(requestMessage.getClient(),
                "Game Request", players[0] + " has challenged you to a game of `" 
                + gameName + "`. Will you accept?").build());
        
        TimerTask task = new TimerTask() {        
            @Override
            public void run() {
                BotUtils.editMessage(requestMessage, BotUtils.getBuilder(requestMessage.getClient(),
                "Game Request", players[1] + " failed to respond to the request in time.").build());
                BotUtils.removeAllReactions(requestMessage);
                GameManager.removeGameRequest(requestMessage);
            }            
        };
        inactiveTimer.schedule(task, 600000L); //10 minutes        
    }   
    
    public void handleMessageReaction(IReaction reaction, IUser fromUser) {
        if (fromUser.equals(players[1])) {
            Button button = bm.getButton(reaction);
            if (button != null) {
                if (button.equals(Button.EXIT)) {
                    BotUtils.editMessage(requestMessage, players[0].mention(), BotUtils.getBuilder(requestMessage.getClient(),
                            "Game Request", "Oh shit. " + fromUser + " has denied your request.").build());
                    BotUtils.removeAllReactions(requestMessage);
                    GameManager.removeGameRequest(requestMessage);
                } else if (button.equals(Button.CHECKMARK)) {
                    BotUtils.removeAllReactions(requestMessage);
                    createGame();                   
                    GameManager.removeGameRequest(requestMessage);
                }
            }
            inactiveTimer.cancel();
        }
        BotUtils.removeMessageReaction(requestMessage, fromUser, reaction);
    }
    
    public void createGame() {
        try {
            BotUtils.editMessage(requestMessage, "Loading `" + gameName + "`..");
            AbstractGame game = gameType.getConstructor( //dont know how else to do this?
                    IMessage.class, IUser[].class).newInstance(requestMessage, players);             
            GameManager.addGame(requestMessage, game);
            game.start();
        } catch (NoSuchMethodException
                | InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
    }
    
}
