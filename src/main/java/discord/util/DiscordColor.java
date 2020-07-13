package discord.util;

import java.awt.*;

public enum DiscordColor {

    RED, GREEN, YELLOW, CYAN, ORANGE, PINK, PURPLE;

    public Color getColor() {
        switch (this) {
            case RED:
                return Color.decode("#F04747"); //DND
            case GREEN:
                return Color.decode("#43B581"); //Online
            case YELLOW:
                return Color.decode("#FAA61A"); //Idle
            case CYAN:
                return Color.decode("#44DDBF"); //Balance
            case ORANGE:
                return Color.decode("#F57731"); //High
            case PINK:
                return Color.decode("#FF73FA"); //Nitro
            case PURPLE:
                return Color.decode("#9B84EE"); //Bravery
        }
        return Color.WHITE;
    }

}
