package discord.manager;

import discord.data.object.Unlockable;
import discord.data.object.user.DUser;

import java.util.*;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Role;

public class ColorManager {

    //Hardcoded names/level unlocks
    //GUILD is responsible for creating roles for each color name
    //User objects actually don't store Unlockable objects
    public static final Unlockable[] COLORS_DEFAULT = {
            Unlockable.color("Red", 1, 0),
            Unlockable.color("Orange", 1, 0),
            Unlockable.color("Yellow", 1, 0),
            Unlockable.color("Green", 1, 0),
            Unlockable.color("Cyan", 1, 0),
            Unlockable.color("Blue", 1, 0),
            Unlockable.color("Purple", 1, 0),
            Unlockable.color("Magenta", 1, 0),
            Unlockable.color("Grey", 1, 0)
    };

    public static final Unlockable[] COLORS_UNLOCKS = {
            Unlockable.color("Forest", 1, 20),
            Unlockable.color("Vermilion", 1, 40),
            Unlockable.color("Bubble Gum", 1, 60),
            Unlockable.color("Azure", 1, 80),
            Unlockable.color("Lime", 2, 20),
            Unlockable.color("Heliotrope", 2, 40),
            Unlockable.color("Teal", 2, 60),
            Unlockable.color("Blood", 2, 80),
            Unlockable.color("Persimmon", 3, 20),
            Unlockable.color("Conifer", 3, 40),
            Unlockable.color("Orchid", 3, 60),
            Unlockable.color("Sunrise", 3, 80),
            Unlockable.color("Indochine", 4, 20),
            Unlockable.color("Turquoise", 4, 40),
            Unlockable.color("Cornflower", 4, 60),
            Unlockable.color("Flesh", 4, 80),
            Unlockable.color("Flamingo", 5, 20),
            Unlockable.color("Aquamarine", 5, 40),
            Unlockable.color("Gold", 5, 60),
            Unlockable.color("Ice", 5, 80),
            Unlockable.color("Olive", 6, 20),
            Unlockable.color("Violet", 6, 40),
            Unlockable.color("Azalea", 6, 60),
            Unlockable.color("Swiss Coffee", 6, 80),
            Unlockable.color("Hopbush", 7, 20),
            Unlockable.color("Discord", 7, 40),
            Unlockable.color("Holy Light", 7, 60),
            Unlockable.color("Scorpion", 7, 80),
            Unlockable.color("Quicksand", 8, 20),
            Unlockable.color("Mauve", 8, 40),
            Unlockable.color("Malibu", 8, 60),
            Unlockable.color("Swan", 8, 80),
            Unlockable.color("Salmon", 9, 20),
            Unlockable.color("Mint", 9, 40),
            Unlockable.color("Blizzard", 9, 60),
            Unlockable.color("Void", 9, 80)
    };

    public static Unlockable[] getDefaultColors() {
        return COLORS_DEFAULT;
    }

    public static Unlockable[] getUnlockableColors() {
        return COLORS_UNLOCKS;
    }

    public static Unlockable[] getUnlockedColorsForDUser(DUser user) {
        List<Unlockable> unlockedColors = new ArrayList<>(Arrays.asList(COLORS_UNLOCKS));
        unlockedColors.removeIf(color -> !user.hasUnlocked(color));
        return unlockedColors.toArray(new Unlockable[unlockedColors.size()]);
    }

    public static Unlockable getUnlockedColor(int totalLevelsRequired) {
        for (Unlockable color : COLORS_UNLOCKS) { //Sue me
            if (color.getTotalLevelRequired() == totalLevelsRequired)
                return color;
        }
        return null;
    }

    public static Unlockable getColor(String name) {
        List<Unlockable> allColors = new ArrayList<>(Arrays.asList(COLORS_DEFAULT));
        allColors.addAll(Arrays.asList(COLORS_UNLOCKS));
        for (Unlockable color : allColors) { //Sue me again
            if (color.toString().equalsIgnoreCase(name)) {
                return color;
            }
        }
        return null;
    }

    public static boolean colorIsInArray(Unlockable[] colors, String name) {
        for (Unlockable c : colors) {
            if (c.toString().equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    public static boolean isDefaultColor(String name) {
        return colorIsInArray(COLORS_DEFAULT, name);
    }

    public static boolean isUnlockedColor(String name) {
        return isDefaultColor(name) || colorIsInArray(COLORS_UNLOCKS, name);
    }

    public static Set<Snowflake> getMemberRolesNoColor(Member member) {
        return new HashSet<>(member.getRoles()
                .filter(role -> !(ColorManager.isUnlockedColor(role.getName()) || ShopManager.isPurchasedColor(role.getName())))
                .map(Role::getId).collectList().block());
    }

}
