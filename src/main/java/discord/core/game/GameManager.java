package discord.core.game;

import discord.util.BotUtils;
import java.util.HashMap;
import java.util.List;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IReaction;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.StatusType;
import sx.blah.discord.util.RequestBuffer;

public class GameManager {

    private static final HashMap<IMessage, GameRequest> REQUESTS = new HashMap<>();
    private static final HashMap<IMessage, AbstractGame> GAMES = new HashMap<>();

    public static void addGameRequest(IMessage requestMessage, GameRequest request) {
        REQUESTS.put(requestMessage, request);
    }

    public static void addGame(IMessage gameMessage, AbstractGame game) {
        GAMES.put(gameMessage, game);
    }
    
    public static void removeGameRequest(IMessage requestMessage) {
        REQUESTS.remove(requestMessage);
    }
    
    public static void removeGame(IMessage gameMessage) {
        GAMES.remove(gameMessage);
    }
    
    public static void handleMessageReaction(IMessage message, IReaction reaction, IUser user) {
        if (GAMES.containsKey(message) && (GAMES.get(message) instanceof ButtonGame)) {
            ButtonGame game = (ButtonGame) GAMES.get(message);
            game.handleMessageReaction(reaction, user);
        } else if (REQUESTS.containsKey(message)) {
            GameRequest request = REQUESTS.get(message);
            request.handleMessageReaction(reaction, user);           
        }
    }
    
    public static void processGameCommand(IMessage message, String gameName, Class<? extends AbstractGame> gameType) {
        List<IUser> opponentList = message.getMentions();
        if (opponentList.isEmpty()) {
            BotUtils.sendErrorMessage(message.getChannel(), "Could not parse a valid opponent. Please @mention them.");
            return;
        }
        
        if (opponentList.get(0).isBot() || opponentList.get(0).equals(message.getAuthor())) {
            BotUtils.sendErrorMessage(message.getChannel(), "You can't play against yourself or a bot.");
            return;
        }
        
        if (opponentList.get(0).getPresence().getStatus().equals(StatusType.OFFLINE)) {
            BotUtils.sendErrorMessage(message.getChannel(), "Your opponent must be online.");
            return;
        }
        
        IMessage requestMessage = RequestBuffer.request(() -> message.getChannel().sendMessage("Loading request..")).get();
        GameManager.addGameRequest(requestMessage, new GameRequest(gameName, gameType, requestMessage, 
                new IUser[] {message.getAuthor(), opponentList.get(0)}));       
    }
}
