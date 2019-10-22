package discord.data.object.user;

public enum Pref {
    MENTION_RANKUP, NOTIFY_UNLOCK, AUTO_PRESTIGE;
    
    public static boolean contains(String value) {
        for (Pref pref : Pref.values()) {
            if (pref.toString().equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
