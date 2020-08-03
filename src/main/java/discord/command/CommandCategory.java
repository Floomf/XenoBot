package discord.command;

public enum CommandCategory {

    ADMIN("🛡️"), INFO("📜"), PERK("💎"), UTILITY("🔧"), FUN("🎉"), GAME("🎲"), HIDDEN("");

    private final String emoji;

    CommandCategory(String emoji) {
        this.emoji = emoji;
    }

    @Override
    public String toString() {
        return super.name().charAt(0) + super.name().substring(1).toLowerCase() + " " + emoji;
    }
}
