package discord.core.game;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;

public abstract class TypeGame extends AbstractGame {

    public TypeGame(Message message, Member[] players, int betAmount) {
        super(message, players, betAmount);
    }

    //Games are responsible for calling updateMessageDisplay() each turn
    abstract protected void onTurn(String input);

    //bad design?
    protected void onEnd(){};

    abstract protected boolean isValidInput(String input);

    @Override
    protected final void setup() {
        super.getGameMessage().getClient().getEventDispatcher().on(MessageCreateEvent.class)
                .takeUntil(active -> !super.isActive())
                .subscribe(this::onMessageCreateEvent);
    }

    private void onMessageCreateEvent(MessageCreateEvent event) {
        Message userMessage = event.getMessage();
        if (super.isActive() && userMessage.getChannelId().equals(super.getGameMessage().getChannelId())) { //has to be in guild
            Member fromMember = userMessage.getAuthorAsMember().block();
            if (super.playerIsInGame(fromMember)) {
                if (userMessage.getContent().equalsIgnoreCase("forfeit")
                    || userMessage.getContent().equalsIgnoreCase("ff")) {
                    userMessage.delete().block();
                    win(getForfeitMessage(fromMember), super.getOtherPlayer(fromMember));
                    return;
                }

                if (fromMember.equals(super.getPThisTurn())) {
                    String input = userMessage.getContent().toLowerCase().trim();
                    if (isValidInput(input)) {
                        onTurn(input);
                        if (super.isActive()) { //kinda messy
                            setupNextTurn();
                        }
                    } else {
                        Message invalidMessage = userMessage.getChannel().block().createMessage("`Invalid input.`").block();
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        invalidMessage.delete().block();
                    }
                }
                userMessage.delete().block();
            }
        }
    }

}

