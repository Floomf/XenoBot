package discord.command.game.slots;

import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.manager.GameManager;
import discord.manager.UserManager;
import discord.util.BotUtils;
import discord.util.MessageUtils;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;

public class SlotsCommand extends AbstractCommand {

    public SlotsCommand() {
        super(new String[]{"slots", "slotmachine"}, 1, CommandCategory.GAME);
    }

    @Override
    public void execute(Message message, TextChannel channel, String[] args) {
        int betAmount = 0;

        try {
            betAmount = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            MessageUtils.sendErrorMessage(channel, "Couldn't parse a valid bet amount.");
            return;
        }

        if (betAmount < 25) {
            MessageUtils.sendErrorMessage(channel, "Minimum bet is **$25**.");
            return;
        } else if (betAmount > 1000) {
            MessageUtils.sendErrorMessage(channel, "Maximum bet is **$1000**.");
            return;
        } else if (betAmount > UserManager.getDUserFromMessage(message).getBalance()) {
            MessageUtils.sendErrorMessage(channel, "You don't have that much money to bet!");
            return;
        }

        GameManager.createSinglePlayerGame(GameSlots.class, "Slots 🎰", channel, message.getAuthorAsMember().block(), betAmount);
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "[bet]", "Spin at the slot machine and wager money.");
    }

}
