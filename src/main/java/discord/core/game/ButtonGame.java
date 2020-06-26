package discord.core.game;

import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;

public abstract class ButtonGame extends AbstractGame {

    private final ButtonManager buttonManager;

    public ButtonGame(Message message, Member[] players, int betAmount) {
        super(message, players, betAmount);
        this.buttonManager = new ButtonManager();
    }

    //Games are responsible for calling updateMessageDisplay() each turn
    abstract protected void onTurn(int input);

    abstract protected boolean isValidInput(int input);

    @Override
    protected final void setup() {
        if (!super.getPThisTurn().equals(super.getPNextTurn())) { //hack to check for multi player game
            buttonManager.addButton(super.getGameMessage(), Button.EXIT);
        }
    }

    @Override
    protected final void onEnd() {
        super.getGameMessage().removeAllReactions().block();
    }

    public final void handleMessageReaction(ReactionEmoji reaction, Member fromUser) {
        if (super.isActive()) {
            Button button = buttonManager.getButton(reaction);
            if (button != null) {
                if (button.equals(Button.EXIT) && super.playerIsInGame(fromUser)) {
                    win(getForfeitMessage(fromUser), super.getOtherPlayer(fromUser));
                } else if (isValidInput(button.getNumValue()) && fromUser.equals(super.getPThisTurn())) {
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

