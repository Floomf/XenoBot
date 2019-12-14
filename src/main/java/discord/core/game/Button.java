package discord.core.game;

import discord4j.core.object.reaction.ReactionEmoji;

public enum Button {

    ONE(ReactionEmoji.unicode("\u0031\u20E3"), 1),
    TWO(ReactionEmoji.unicode("\u0032\u20E3"), 2),
    THREE(ReactionEmoji.unicode("\u0033\u20E3"), 3),
    FOUR(ReactionEmoji.unicode("\u0034\u20E3"), 4),
    FIVE(ReactionEmoji.unicode("\u0035\u20E3"), 5),
    SIX(ReactionEmoji.unicode("\u0036\u20E3"), 6),
    SEVEN(ReactionEmoji.unicode("\u0037\u20E3"), 7),
    EIGHT(ReactionEmoji.unicode("\u0038\u20E3"), 8),

    CHECKMARK(ReactionEmoji.unicode("\u2705"), 0),
    EXIT(ReactionEmoji.unicode("\uD83D\uDEAB"), 0);

    private static Button[] cachedValues = null;
    private final ReactionEmoji emoji;
    private final int numValue;

    Button(ReactionEmoji emoji, int numericValue) {
        this.emoji = emoji;
        this.numValue = numericValue;
    }

    public static Button getFromNum(int num) {
        if (cachedValues == null) {
            cachedValues = Button.values();
        }
        return cachedValues[num - 1];
    }

    public ReactionEmoji getEmoji() {
        return emoji;
    }

    public int getNumValue() {
        return numValue;
    }

}

