package discord.core.game;

import discord.core.game.AbstractGame;
import discord.util.BotUtils;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.RequestBuffer;

public abstract class TypeGame extends AbstractGame {

    public TypeGame(IMessage message, IUser[] players) {
        super(message, players);
    }

    //Games are responsible for calling updateMessageDisplay() each turn
    abstract protected void onTurn(String input);

    abstract protected boolean isValidInput(String input);

    @Override
    protected final void setup() {
        super.getGameMessage().getClient().getDispatcher().registerListener(this);
    }

    @Override
    protected final void onEnd() {
        super.getGameMessage().getClient().getDispatcher().unregisterListener(this);
    }
    
    @EventSubscriber
    public void onMessageEvent(MessageReceivedEvent event) throws InterruptedException {
        IMessage userMessage = event.getMessage();
        IUser fromUser = userMessage.getAuthor();
        if (userMessage.getChannel().equals(super.getGameMessage().getChannel())) {
            if (userMessage.getContent().equalsIgnoreCase("forfeit") && (fromUser.equals(super.getThisTurnUser()) 
                    || fromUser.equals(super.getNextTurnUser()))) {
                win(fromUser, fromUser.getName() + " forfeits. " + super.getOtherUser(fromUser).getName() + " wins!");
            }
            
            if (userMessage.getAuthor().equals(super.getThisTurnUser())) {
                String input = userMessage.getContent().toLowerCase().trim();
                BotUtils.deleteMessage(userMessage);
                if (isValidInput(input)) {
                    onTurn(input);
                    if (super.isActive()) {
                        setupNextTurn();
                    }
                } else {
                    IMessage invalidMessage = RequestBuffer.request(
                            () -> userMessage.getChannel().sendMessage("Invalid position.")).get();
                    Thread.sleep(2000);
                    BotUtils.deleteMessage(invalidMessage);
                }
            } else if (userMessage.getAuthor().equals(super.getNextTurnUser())) {
                BotUtils.deleteMessage(userMessage);
            }
        }
    }

}
