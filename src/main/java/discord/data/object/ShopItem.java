package discord.data.object;

public enum ShopItem {

    JAZZBERRY("Jazzberry", 2000, Category.NAME_COLOR, 0),
    CINNAMON("Cinnamon", 2000, Category.NAME_COLOR, 0),
    JUNGLE("Jungle", 2000, Category.NAME_COLOR, 0),
    JEWEL("Jewel", 3000, Category.NAME_COLOR, 0),
    DARK_SEAFOAM("Dark Seafoam", 3000, Category.NAME_COLOR, 0),
    PERIWINKLE("Periwinkle", 3000, Category.NAME_COLOR, 0),
    LYNCH("Lynch", 4000, Category.NAME_COLOR, 0),
    EMINENCE("Eminence", 4000, Category.NAME_COLOR, 0),
    STRATOS("Stratos", 4000, Category.NAME_COLOR, 0),
    DOUBLE_XP_1H("Double XP for 1h", 500, Category.XP_MULTIPLIER, 1),
    DOUBLE_XP_3H("Double XP for 3h", 1250, Category.XP_MULTIPLIER, 3),
    TRIPLE_XP_5H("Triple XP for 5h", 2500, Category.XP_MULTIPLIER, 5),

    SERVER_EMOJI("Custom Server Emoji", 5000, Category.MISC, 0),
    EXTENDED_EMOJIS("Extended Length For `/emoji`", 4000, Category.MISC, 0);

    public enum Category {
        NAME_COLOR("Name/Poll Colors 🌈"), XP_MULTIPLIER("Temporary XP Multipliers ⏲️"),
        MISC("Miscellaneous ❓");

        private final String name;

        Category(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private final String name;
    private final int price;
    private final Category category;
    private final int duration;

    ShopItem(String name, int price, Category category, int duration) {
        this.name = name;
        this.price = price;
        this.category = category;
        this.duration = duration;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public Category getCategory() {
        return category;
    }

    public int getDuration() {
        return duration;
    }

}

