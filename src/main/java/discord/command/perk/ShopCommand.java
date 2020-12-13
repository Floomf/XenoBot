package discord.command.perk;

import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.manager.ShopManager;
import discord.manager.UserManager;
import discord.data.object.ShopItem;
import discord.data.object.user.DUser;
import discord.util.BotUtils;
import discord.util.MessageUtils;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;

public class ShopCommand extends AbstractCommand {

    public ShopCommand() {
        super(new String[]{"shop", "store"}, 0, CommandCategory.PERK);
    }

    @Override
    public void execute(Message message, TextChannel channel, String[] args) {
        if (args.length == 2 && args[0].equalsIgnoreCase("buy")) {
            if (!args[1].matches("\\d{1,2}")) { //one or two digits
                MessageUtils.sendErrorMessage(channel, "Couldn't parse an item index to purchase from.");
                return;
            }
            ShopItem item = ShopManager.getShopItem(Integer.parseInt(args[1]));

            if (item == null) {
                MessageUtils.sendErrorMessage(channel, "An item by that index doesn't exist.");
                return;
            }

            DUser dUser = UserManager.getDUserFromMessage(message);

            if (dUser.hasPurchased(item)) {
                MessageUtils.sendErrorMessage(channel, "You've already purchased that!");
                return;
            }

            if (!dUser.canPurchase(item)) {
                MessageUtils.sendErrorMessage(channel, "You can't afford to purchase that!");
                return;
            }
            dUser.purchase(item);
            MessageUtils.sendInfoMessage(channel, "Successfully purchased. Enjoy!");
            UserManager.saveDatabase();
        } else {
            channel.createEmbed(ShopManager.getShopEmbedFor(UserManager.getDUserFromMessage(message))).block();
        }
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "", "View the shop.");
    }

}
