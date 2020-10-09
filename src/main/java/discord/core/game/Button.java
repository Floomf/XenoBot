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

    D(ReactionEmoji.unicode("\uD83C\uDDE9"), 0),
    H(ReactionEmoji.unicode("\uD83C\uDDED"), 0),
    N(ReactionEmoji.unicode("\uD83C\uDDF3"), 0),
    P(ReactionEmoji.unicode("\uD83C\uDDF5"), 0),
    S(ReactionEmoji.unicode("\uD83C\uDDF8"), 0),
    U(ReactionEmoji.unicode("\uD83C\uDDFA"), 0),
    Y(ReactionEmoji.unicode("\uD83C\uDDFE"), 0),

    LEFT_ARROW(ReactionEmoji.unicode("⬅️"), 0),

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

