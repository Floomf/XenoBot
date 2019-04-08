package discord.data.object;

public enum Pref {
    MENTION_RANKUP, AUTO_PRESTIGE;
    
    public static boolean contains(String value) {
        for (Pref pref : Pref.values()) {
            if (pref.toString().equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }
    
    public String toString() {
        return super.toString().toLowerCase();
    }
}
