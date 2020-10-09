package discord.core.game;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;

public abstract class TypeGame extends AbstractGame {

    static long time;

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
                .takeUntil(e -> !super.isActive())
                .filter(e -> e.getMessage().getChannelId().equals(getGameMessage().getChannelId())
                        && playerIsInGame(e.getMember().get()))
                .subscribe(e -> onPlayerMessage(e.getMessage(), e.getMember().get()));
    }

    private void onPlayerMessage(Message playerMessage, Member player) {
        if (playerMessage.getContent().equalsIgnoreCase("forfeit")
                || playerMessage.getContent().equalsIgnoreCase("ff")) {
            playerMessage.delete().block();
            win(getForfeitMessage(player), super.getOtherPlayer(player));
            return;
        }

        playerMessage.delete().block();
        if (player.equals(super.getPThisTurn())) {
            String input = playerMessage.getContent().toLowerCase().trim();
            if (isValidInput(input)) {
                onTurn(input);
                if (super.isActive()) { //kinda messy
                    setupNextTurn();
                }
            } else {
                Message invalidMessage = playerMessage.getChannel().block().createMessage("**Invalid input.**\n(You can use **ff** to forfeit)").block();
                try {
                    Thread.sleep(1250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                invalidMessage.delete().block();
            }
        }
    }

    /*
    private void onMessageCreateEvent(Message playerMessage, Member player) {
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
    }*/

}

