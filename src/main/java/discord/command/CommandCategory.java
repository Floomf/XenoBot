package discord.command;

public enum CommandCategory {
    ADMIN, INFO, PERK, UTILITY, FUN, GAME, HIDDEN;

    @Override
    public String toString() {
        String temp = super.toString();
        temp = temp.charAt(0) + temp.substring(1).toLowerCase();
        switch (this) {
            case ADMIN:
                return temp + " 🛡️";
            case INFO:
                return temp + " 📜";
            case PERK:
                return temp + " 💎";
            case UTILITY:
                return temp + " 🔧";
            case FUN:
                return temp + " 🎉";
            case GAME:
                return temp + " 🎲";
            default:
                return temp;
        }
    }
}
