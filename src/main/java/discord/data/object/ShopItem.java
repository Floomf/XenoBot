package discord.data.object;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ShopItem {

    public enum Category {
        NAME_COLOR, XP_MULTIPLIER, MISC;

        @Override
        public String toString() {
            if (this == Category.NAME_COLOR) {
                return "Name Colors 🌈";
            } else if (this == XP_MULTIPLIER) {
                return "Personal XP Boosts 🚀";
            } else if (this == Category.MISC) {
                return "Miscellaneous ❓";
            }
            return "???";
        }
    }

    private final String name;
    private final int price;
    private final Category category;

    public ShopItem(@JsonProperty("name") String name,
                    @JsonProperty("price") int price,
                    @JsonProperty("category") Category category) {
        this.name = name;
        this.price = price;
        this.category = category;
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

}

