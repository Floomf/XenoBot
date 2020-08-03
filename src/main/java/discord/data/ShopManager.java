package discord.data;

import discord.data.object.ShopItem;
import discord.data.object.TempShopItem;
import discord.data.object.user.DUser;
import discord.util.DiscordColor;
import discord.util.MessageUtils;
import discord4j.core.object.entity.Role;
import discord4j.core.spec.EmbedCreateSpec;

import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

public class ShopManager {

    private static final ShopItem[] SHOP_ITEMS = {
            new ShopItem("Jazzberry", 1000, ShopItem.Category.NAME_COLOR),
            new ShopItem("Cinnamon", 1000, ShopItem.Category.NAME_COLOR),
            new ShopItem("Jungle", 1000, ShopItem.Category.NAME_COLOR),
            new ShopItem("Jewel", 1500, ShopItem.Category.NAME_COLOR),
            new ShopItem("Dark Seafoam", 1500, ShopItem.Category.NAME_COLOR),
            new ShopItem("Periwinkle", 1500, ShopItem.Category.NAME_COLOR),
            new ShopItem("Lynch", 2000, ShopItem.Category.NAME_COLOR),
            new ShopItem("Eminence", 2000, ShopItem.Category.NAME_COLOR),
            new ShopItem("Stratos", 2000, ShopItem.Category.NAME_COLOR),

            new TempShopItem("Double XP for 1h", 500, ShopItem.Category.XP_MULTIPLIER, 1),
            new TempShopItem("Double XP for 3h", 1000, ShopItem.Category.XP_MULTIPLIER, 3),
            new TempShopItem("Triple XP for 5h", 2500, ShopItem.Category.XP_MULTIPLIER, 5),

            new ShopItem("Custom Server Emoji", 2000, ShopItem.Category.MISC),
            new ShopItem("Custom Server Role", 5000, ShopItem.Category.MISC)
    };

    public static Consumer<EmbedCreateSpec> getShopEmbedFor(DUser dUser) {
        //there are some temp final variables, but how else to we do it when using lambdas?
        Consumer<EmbedCreateSpec> embed = MessageUtils.getEmbed("The Realm Shop 🛒",
                "💰 Balance: **" + String.format("$%,d", dUser.getBalance())+"**\n*To buy an item, use* **`!shop buy [number]`**",
                DiscordColor.PURPLE);
        List<Role> roles = dUser.asGuildMember().getGuild().block().getRoles().collectList().block();

        ShopItem.Category currentCat = SHOP_ITEMS[0].getCategory();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < SHOP_ITEMS.length; i++) {
            ShopItem currentItem = SHOP_ITEMS[i];
            if (currentItem.getCategory() != currentCat) {
                final ShopItem.Category finalCurrentCat = currentCat;
                final String finalItemList = sb.toString(); //has to be stored now otherwise sb.toString() will be called later
                embed = embed.andThen(embedSpec -> embedSpec.addField(finalCurrentCat.toString(), finalItemList, false));
                sb.setLength(0);
            }
            currentCat = currentItem.getCategory();
            sb.append("**").append(i + 1).append(")** ")
                    .append(currentItem.getCategory() == ShopItem.Category.NAME_COLOR
                            ? getRoleFromList(currentItem.getName(), roles).getMention()
                            : currentItem.getName())
                    .append(" - **")
                    .append(dUser.hasPurchased(currentItem) ? "Purchased" : String.format("$%,d", currentItem.getPrice()))
                    .append("**\n");
        }
        ShopItem.Category finalCurrentCat = currentCat;
        embed = embed.andThen(embedSpec -> embedSpec.addField(finalCurrentCat.toString(), sb.toString(), false));

        return embed;
    }

    public static ShopItem getShopItem(int index) {
        if (index > SHOP_ITEMS.length || index < 1) {
            return null;
        }
        return SHOP_ITEMS[index - 1];
    }

    public static boolean isPurchasedColor(String name) {
        for (ShopItem item : SHOP_ITEMS) {
            if (item.getName().equalsIgnoreCase(name)
                    && item.getCategory() == ShopItem.Category.NAME_COLOR) {
                return true;
            }
        }
        return false;
    }

    private static Role getRoleFromList(String name, List<Role> roles) {
        return roles.stream().filter(role -> role.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

}
