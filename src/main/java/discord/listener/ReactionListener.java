package discord.listener;

import discord.core.game.GameManager;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent;

public class ReactionListener {
    
    @EventSubscriber
    public void onReactionAddEvent(ReactionAddEvent event) {
        if (!event.getUser().isBot()) {
            GameManager.handleMessageReaction(event.getMessage(), event.getReaction(), 
                    event.getUser());
        }
    }
    
}
