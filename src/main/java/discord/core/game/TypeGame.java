package discord.core.game;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;

public abstract class TypeGame extends AbstractGame {

    public TypeGame(Message message, Member[] players) {
        super(message, players);
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
        if (super.isActive() && userMessage.getChannel().block().equals(super.getGameMessage().getChannel().block())) { //has to be in guild
            Member fromMember = userMessage.getAuthorAsMember().block();
            if (fromMember.equals(super.getThisTurnUser()) || fromMember.equals(super.getNextTurnUser())) {
                if (userMessage.getContent().orElse("").equalsIgnoreCase("forfeit")) {
                    userMessage.delete().block();
                    win(super.getOtherUser(fromMember), fromMember.getDisplayName() + " forfeits.\n" + super.getOtherUser(fromMember).getMention() + " wins!");
                    return;
                }

                if (userMessage.getAuthor().get().equals(super.getThisTurnUser())) {
                    String input = userMessage.getContent().orElse("").toLowerCase().trim();
                    userMessage.delete().block();
                    if (isValidInput(input)) {
                        onTurn(input);
                        if (super.isActive()) { //kinda messy
                            setupNextTurn();
                        }
                    } else {
                        Message invalidMessage = userMessage.getChannel().block().createMessage("**Invalid input.**").block();
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        invalidMessage.delete().block();
                    }
                }
            }
        }
    }

}

