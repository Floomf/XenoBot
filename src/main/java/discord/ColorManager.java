package discord;

import discord.object.Unlockable;
import discord.object.User;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ColorManager {

    //Hardcoded names/level unlocks
    //guild is responsible for creating roles for each color name
    //User objects actually don't store a Unlockable object
    public static final Unlockable COLORS_DEFAULT[] = {
        new Unlockable("Red", 1, 0),
        new Unlockable("Orange", 1, 0),
        new Unlockable("Yellow", 1, 0),
        new Unlockable("Green", 1, 0),
        new Unlockable("Cyan", 1, 0),
        new Unlockable("Blue", 1, 0),
        new Unlockable("Purple", 1, 0),
        new Unlockable("Magenta", 1, 0),
        new Unlockable("Grey", 1, 0)
    };
    
    public static final Unlockable COLORS_UNLOCKS[] = {
        new Unlockable("Forest", 1, 20),
        new Unlockable("Vermilion", 1, 40),
        new Unlockable("Bubble Gum", 1, 60),
        new Unlockable("Azure", 1, 80),
        new Unlockable("Lime", 2, 20),
        new Unlockable("Heliotrope", 2, 40),
        new Unlockable("Teal", 2, 60),
        new Unlockable("Blood", 2, 80),
        new Unlockable("Persimmon", 3, 20),
        new Unlockable("Conifer", 3, 40),
        new Unlockable("Orchid", 3, 60),
        new Unlockable("Sunrise", 3, 80),
        new Unlockable("Indochine", 4, 20),
        new Unlockable("Turquoise", 4, 40),
        new Unlockable("Cornflower", 4, 60),
        new Unlockable("Flesh", 4, 80),        
        new Unlockable("Flamingo", 5, 20),
        new Unlockable("Aquamarine", 5, 40),
        new Unlockable("Gold", 5, 60),
        new Unlockable("Ice", 5, 80),
        new Unlockable("Olive", 6, 20),
        new Unlockable("Violet", 6, 40),
        new Unlockable("Azalea", 6, 60),
        new Unlockable("Swiss Coffee", 6, 80),
        new Unlockable("Hopbush", 7, 20),
        new Unlockable("Discord", 7, 40),
        new Unlockable("Holy Light", 7, 60),
        new Unlockable("Scorpion", 7, 80),
        new Unlockable("Quicksand", 8, 20),
        new Unlockable("Malibu", 8, 40),
        new Unlockable("Mint", 8, 60),
        new Unlockable("Swan", 8, 80),
        new Unlockable("Salmon", 9, 20),
        new Unlockable("Blizzard", 9, 40),
        new Unlockable("Mauve", 9, 60),
        new Unlockable("Void", 9, 80)        
    };
    
    public static Unlockable[] getDefaultColors() {
        return COLORS_DEFAULT;
    }
    
    public static Unlockable[] getUnlockedColorsForUser(User user) {
        List<Unlockable> unlockedColors = new ArrayList<>(Arrays.asList(COLORS_UNLOCKS));
        unlockedColors.removeIf(color -> !user.hasUnlocked(color));
        return unlockedColors.toArray(new Unlockable[unlockedColors.size()]);
    }
    
    public static Unlockable getUnlockedColor(int totalLevelsRequired) {
       for (Unlockable color : COLORS_UNLOCKS) { //Sue me 
           if (color.getTotalLevelsRequired() == totalLevelsRequired)
               return color;
       }
       return null;
    }
    
    public static Unlockable getColor(String name) {
        List<Unlockable> allColors = new ArrayList<>(Arrays.asList(COLORS_DEFAULT));
        allColors.addAll(Arrays.asList(COLORS_UNLOCKS));
        for (Unlockable color : allColors) { //Sue me again
            if (color.toString().toLowerCase().equals(name.toLowerCase())) {
                return color;
            }
        }
        return null;
    }
    
}
