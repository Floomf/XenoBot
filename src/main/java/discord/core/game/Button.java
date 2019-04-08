package discord.core.game;

import sx.blah.discord.handle.impl.obj.ReactionEmoji;

public enum Button {        
        
    ONE (ReactionEmoji.of("\u0031\u20E3"), 1),
    TWO (ReactionEmoji.of("\u0032\u20E3"), 2),
    THREE (ReactionEmoji.of("\u0033\u20E3"), 3),
    FOUR (ReactionEmoji.of("\u0034\u20E3"), 4),
    FIVE (ReactionEmoji.of("\u0035\u20E3"), 5),
    SIX (ReactionEmoji.of("\u0036\u20E3"), 6),
    SEVEN (ReactionEmoji.of("\u0037\u20E3"), 7),
    EIGHT (ReactionEmoji.of("\u0038\u20E3"), 8),
    
    CHECKMARK (ReactionEmoji.of("\u2705"), 0),
    EXIT (ReactionEmoji.of("\uD83D\uDEAB"), 0);
      
    private final ReactionEmoji emoji;
    private final int numValue;
    
    private static Button[] cachedValues = null;
    
    private Button(ReactionEmoji emoji, int numericValue) {
        this.emoji = emoji;
        this.numValue = numericValue;
    }
    
    public ReactionEmoji getEmoji() {
        return emoji;
    }
    
    public int getNumValue() {
        return numValue;
    }
    
    public static Button getFromNum(int num) {
        if (cachedValues == null) {
            cachedValues = Button.values();
        }
        return cachedValues[num - 1];
    }
    
}
