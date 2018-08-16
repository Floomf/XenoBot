package discord.command;

public enum CommandCategory {
    ADMIN, FUN, HIDDEN, INFO, PERK, UTILITY;

    @Override
    public String toString() {
        String temp = super.toString();
        return temp.charAt(0) + temp.substring(1).toLowerCase();
    }
}
