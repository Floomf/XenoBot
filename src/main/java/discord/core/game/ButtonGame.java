package discord.core.game;

import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;

public abstract class ButtonGame extends AbstractGame {

    private final ButtonManager buttonManager;

    public ButtonGame(Message message, Member[] players) {
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
        super.getGameMessage().removeAllReactions().block();
    }

    public final void handleMessageReaction(ReactionEmoji reaction, Member fromUser) {
        if (super.isActive()) {
            Button button = buttonManager.getButton(reaction);
            if (button != null) {
                if (button.equals(Button.EXIT) && (fromUser.equals(super.getPlayerThisTurn()) || fromUser.equals(super.getPlayerNextTurn()))) {
                    win(getForfeitMessage(fromUser));
                } else if (isValidInput(button.getNumValue()) && fromUser.equals(super.getPlayerThisTurn())) {
                    onTurn(button.getNumValue());
                    if (super.isActive()) {
                        super.setupNextTurn();
                    }
                }
            }
            super.getGameMessage().removeReaction(reaction, fromUser.getId()).block();
        }
    }

    protected final ButtonManager getButtonManager() {
        return buttonManager;
    }

}

