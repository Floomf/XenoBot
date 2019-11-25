package discord.core.game;

import java.util.ArrayList;
import java.util.List;

import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;

public class ButtonManager {

    private final List<Button> buttons = new ArrayList<>();

    public void addNumButtons(Message message, int toNum) {
        for (int i = 1; i <= toNum; i++) {
            addButton(message, Button.getFromNum(i));
        }
    }

    public void addButton(Message message, Button button) {
        buttons.add(button);
        message.addReaction(button.getEmoji()).block();
    }

    public Button getButton(ReactionEmoji reaction) {
        for (Button button : buttons) {
            if (button.getEmoji().equals(reaction))
                return button;
        }
        return null;
    }

}

