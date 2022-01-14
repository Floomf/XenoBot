package discord.command.perk;

import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.core.command.InteractionContext;
import discord.manager.ShopManager;
import discord.manager.UserManager;
import discord.data.object.ShopItem;
import discord.data.object.user.DUser;
import discord.util.BotUtils;
import discord.util.MessageUtils;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.core.object.command.ApplicationCommandOption;

public class ShopCommand extends AbstractCommand {

    public ShopCommand() {
        super("shop", 0, CommandCategory.PERK);
    }

    @Override
    public ApplicationCommandRequest buildSlashCommand() {
        return ApplicationCommandRequest.builder()
                .name(getName())
                .description("View the top users on this server")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("view")
                        .description("View the shop")
                        .type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
                        .build())
                .addOption(ApplicationCommandOptionData.builder()
                        .name("buy")
                        .description("Buy an item from the shop")
                        .type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
                        .addOption(ApplicationCommandOptionData.builder()
                                .name("item_id")
                                .description("The item id")
                                .type(ApplicationCommandOption.Type.INTEGER.getValue())
                                .required(true)
                                .build())
                        .build())
                .build();
    }

    @Override
    public void execute(InteractionContext context) {
        ApplicationCommandInteractionOption sub = context.getSubCommand();
        if (sub.getName().equals("buy")) {
            ShopItem item = ShopManager.getShopItem((int) sub.getOption("item_id").get().getValue().get().asLong());
            if (item == null) {
                context.replyWithError("There's no item with that id!");
                return;
            }
            DUser dUser = context.getDUser();

            if (dUser.hasPurchased(item)) {
                context.replyWithError("You've already purchased that!");
                return;
            }

            if (!dUser.canPurchase(item)) {
                context.replyWithError("You can't afford to purchase that!");
                return;
            }
            dUser.purchase(item);
            context.replyWithInfo("Successfully purchased. Enjoy!");
            UserManager.saveDatabase();
        } else if (sub.getName().equals("view")) {
            context.reply(ShopManager.getShopEmbedFor(context.getDUser()));
        }
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
            channel.createMessage(spec -> {
                spec.addEmbed(ShopManager.getShopEmbedFor(UserManager.getDUserFromMessage(message)));
            }).block();
        }
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "", "View the shop.");
    }

}
