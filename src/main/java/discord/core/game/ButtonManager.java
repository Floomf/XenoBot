package discord.core.game;

import discord.util.BotUtils;
import java.util.ArrayList;
import java.util.List;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IReaction;

public class ButtonManager {
    
    private final List<Button> buttons = new ArrayList<>();

    public void addNumButtons(IMessage message, int toNum) {
        for (int i = 1; i <= toNum; i++) {
            addButton(message, Button.getFromNum(i));
        }
    }
    
    public void addButton(IMessage message, Button button) {
        buttons.add(button);
        BotUtils.addMessageReaction(message, button.getEmoji());
    }
        
    public Button getButton(IReaction reaction) {
       for (Button button : buttons) {
           if (button.getEmoji().equals(reaction.getEmoji()))
               return button;
       }
       return null;
    }
           
}
