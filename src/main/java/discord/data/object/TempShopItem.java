package discord.data.object;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TempShopItem extends ShopItem {

    private final int hours;

    public TempShopItem(@JsonProperty("name") String name,
                        @JsonProperty("price") int price,
                        @JsonProperty("category") Category category,
                        @JsonProperty("hours") int hours) {
        super(name, price, category);
        this.hours = hours;
    }

    public int getHours() {
        return hours;
    }

}
