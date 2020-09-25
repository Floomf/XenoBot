package discord.data.object;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ShopItem {

    public enum Category {
        NAME_COLOR("Name Colors 🌈"), XP_MULTIPLIER("Personal XP Boosts 🚀"), MISC("Miscellaneous ❓");

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

