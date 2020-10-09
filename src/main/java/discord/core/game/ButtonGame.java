package discord.core.game;

import discord4j.core.event.domain.message.ReactionAddEvent;
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
    abstract protected void onTurn(Button input);

    abstract protected boolean isValidInput(Button input);

    @Override
    protected final void setup() {
        if (!super.getPThisTurn().equals(super.getPNextTurn())) { //hack to check for multi player game
            buttonManager.addButton(super.getGameMessage(), Button.EXIT);
        }

        super.getGameMessage().getClient().on(ReactionAddEvent.class)
                .takeUntil(e -> !super.isActive())
                .filter(e -> e.getMessageId().equals(super.getGameMessage().getId()) && !e.getMember().get().isBot())
                .doOnNext(e -> getGameMessage().removeReaction(e.getEmoji(), e.getUserId()).block()) //ideally do after?
                .filter(e -> super.playerIsInGame(e.getMember().get()))
                .subscribe(e -> onPlayerReaction(e.getEmoji(), e.getMember().get()));
    }

    @Override
    protected final void onEnd() {
        super.getGameMessage().removeAllReactions().block();
    }

    public final void onPlayerReaction(ReactionEmoji emoji, Member player) {
        Button button = buttonManager.getButton(emoji);
        if (button != null) {
            if (button.equals(Button.EXIT)) {
                win(getForfeitMessage(player), super.getOtherPlayer(player));
            } else if (player.equals(super.getPThisTurn()) && isValidInput(button)) {
                onTurn(button);
                if (super.isActive()) {
                    super.setupNextTurn();
                }
            }
        }
        //super.getGameMessage().removeReaction(emoji, player.getId()).block();
    }
        /*if (super.isActive()) {
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
        }*/

    protected final ButtonManager getButtonManager() {
        return buttonManager;
    }

}

