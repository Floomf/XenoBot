package discord.core.game;

import discord.util.BotUtils;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IReaction;
import sx.blah.discord.handle.obj.IUser;

public abstract class ButtonGame extends AbstractGame {
    
    private final ButtonManager buttonManager;
    
    public ButtonGame(IMessage message, IUser[] players) {
        super(message, players);
        this.buttonManager = new ButtonManager();
    }
       
    //Games are responsible for calling updateMessageDisplay() each turn
    abstract protected void onTurn(int input);
    
    abstract protected boolean isValidInput(int input);
    
    @Override
    protected final void setup() {
        buttonManager.addButton(super.getGameMessage(), Button.EXIT); 
    }
    
    @Override
    protected final void onEnd() {
        BotUtils.removeAllReactions(super.getGameMessage());
    }
    
    public final void handleMessageReaction(IReaction reaction, IUser fromUser) {
        if (super.isActive()) {
            Button button = buttonManager.getButton(reaction);
            if (button != null) {
                if (button.equals(Button.EXIT) && (fromUser.equals(super.getThisTurnUser()) || fromUser.equals(super.getNextTurnUser()))) {
                    win(fromUser, fromUser.getName() + " forfeits. " + super.getOtherUser(fromUser).getName() + " wins!");
                } else if (isValidInput(button.getNumValue()) && fromUser.equals(super.getThisTurnUser())) {
                    onTurn(button.getNumValue());
                    if (super.isActive()) {
                        super.setupNextTurn();
                    }
                }
            }
            BotUtils.removeMessageReaction(super.getGameMessage(), fromUser, reaction);
        }
    }
       
    protected final ButtonManager getButtonManager() {
        return buttonManager;
    }
         
}
