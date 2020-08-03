package discord.util;

import discord4j.rest.util.Color;

public class DiscordColor {

    public static final Color RED = Color.of(hexToInt("F04747"));
    public static final Color GREEN = Color.of(hexToInt("43B581"));
    public static final Color YELLOW = Color.of(hexToInt("FAA61A"));
    public static final Color CYAN = Color.of(hexToInt("44DDBF"));
    public static final Color ORANGE = Color.of(hexToInt("F57731"));
    public static final Color PINK = Color.of(hexToInt("FF73FA"));
    public static final Color PURPLE = Color.of(hexToInt("9B84EE"));

    private static int hexToInt(String hex) {
        return Integer.parseInt(hex, 16);
    }

}
