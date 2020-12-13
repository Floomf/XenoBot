package discord.core.game;

public class GameEmoji {
    public static final String ONE = "\u0031\u20E3";
    public static final String TWO = "\u0032\u20E3";
    public static final String THREE = "\u0033\u20E3";
    public static final String FOUR = "\u0034\u20E3";
    public static final String FIVE = "\u0035\u20E3";
    public static final String SIX = "\u0036\u20E3";
    public static final String SEVEN = "\u0037\u20E3";

    public static final String D = "\uD83C\uDDE9";
    public static final String H = "\uD83C\uDDED";
    public static final String N = "\uD83C\uDDF3";
    public static final String P = "\uD83C\uDDF5";
    public static final String S = "\uD83C\uDDF8";
    public static final String U = "\uD83C\uDDFA";
    public static final String Y = "\uD83C\uDDFE";

    public static final String LEFT_ARROW = "⬅️";

    public static final String CHECKMARK = "\u2705";
    public static final String EXIT = "\uD83D\uDEAB";

    public static int numberEmojiToInt(String emoji) {
        return emoji.length() == 2 && emoji.charAt(1) == '\u20E3'
                ? emoji.charAt(0) - '\u0030' : 0;
    }


}
