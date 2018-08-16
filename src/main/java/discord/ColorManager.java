package discord;

import discord.object.Color;
import discord.object.User;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ColorManager {

    //Hardcoded names/level unlocks
    //guild is responsible for creating roles for each color name
    //User objects actually don't store a Color object
    public static final Color COLORS_DEFAULT[] = {
        new Color("Red", 1, 0),
        new Color("Orange", 1, 0),
        new Color("Yellow", 1, 0),
        new Color("Green", 1, 0),
        new Color("Cyan", 1, 0),
        new Color("Blue", 1, 0),
        new Color("Purple", 1, 0),
        new Color("Magenta", 1, 0),
        new Color("Grey", 1, 0)
    };
    
    public static final Color COLORS_UNLOCKS[] = {
        new Color("Forest", 1, 20),
        new Color("Vermilion", 1, 40),
        new Color("Bubble Gum", 1, 60),
        new Color("Azure", 1, 80),
        new Color("Lime", 2, 20),
        new Color("Heliotrope", 2, 40),
        new Color("Teal", 2, 60),
        new Color("Blood", 2, 80),
        new Color("Persimmon", 3, 20),
        new Color("Conifer", 3, 40),
        new Color("Orchid", 3, 60),
        new Color("Sunrise", 3, 80),
        new Color("Indochine", 4, 20),
        new Color("Turquoise", 4, 40),
        new Color("Cornflower", 4, 60),
        new Color("Flesh", 4, 80),        
        new Color("Flamingo", 5, 20),
        new Color("Aquamarine", 5, 40),
        new Color("Gold", 5, 60),
        new Color("Ice", 5, 80),
        new Color("Olive", 6, 20),
        new Color("Violet", 6, 40),
        new Color("Azalea", 6, 60),
        new Color("Swiss Coffee", 6, 80),
        new Color("Hopbush", 7, 20),
        new Color("Discord", 7, 40),
        new Color("Holy Light", 7, 60),
        new Color("Scorpion", 7, 80),
        new Color("Quicksand", 8, 20),
        new Color("Malibu", 8, 40),
        new Color("Mint", 8, 60),
        new Color("Swan", 8, 80),
        new Color("Salmon", 9, 20),
        new Color("Blizzard", 9, 40),
        new Color("Mauve", 9, 60),
        new Color("Void", 9, 80)        
    };
    
    public static Color[] getDefaultColors() {
        return COLORS_DEFAULT;
    }
    
    public static Color[] getUnlockedColorsForUser(User user) {
        List<Color> unlockedColors = new ArrayList<>(Arrays.asList(COLORS_UNLOCKS));
        unlockedColors.removeIf(color -> !hasColorUnlocked(user, color));
        return unlockedColors.toArray(new Color[unlockedColors.size()]);
    }
    
    public static Color getUnlockedColor(int totalLevelsRequired) {
       for (Color color : COLORS_UNLOCKS) { //Sue me 
           if (color.getTotalLevelsRequired() == totalLevelsRequired)
               return color;
       }
       return null;
    }
    
    public static Color getColor(String name) {
        List<Color> allColors = new ArrayList<>(Arrays.asList(COLORS_DEFAULT));
        allColors.addAll(Arrays.asList(COLORS_UNLOCKS));
        for (Color color : allColors) { //Sue me again
            if (color.getName().toLowerCase().equals(name.toLowerCase())) {
                return color;
            }
        }
        return null;
    }
    
    public static boolean hasColorUnlocked(User user, Color color) {
        return (user.getTotalLevels() >= color.getTotalLevelsRequired());
    }
    
}
