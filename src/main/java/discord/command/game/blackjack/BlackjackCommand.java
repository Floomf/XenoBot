package discord.command.game.blackjack;

import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.command.game.akinator.GameAkinator;
import discord.core.game.AbstractGame;
import discord.core.game.GameManager;
import discord.data.UserManager;
import discord.util.BotUtils;
import discord.util.MessageUtils;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;

public class BlackjackCommand extends AbstractCommand {

    public BlackjackCommand() {
        super(new String[]{"blackjack", "bj", "21"}, 1, CommandCategory.GAME);
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

        if (betAmount < 10) {
            MessageUtils.sendErrorMessage(channel, "Minimum bet is **$10**.");
            return;
        } else if (betAmount > 500) {
            MessageUtils.sendErrorMessage(channel, "Maximum bet is **$500**.");
            return;
        } else if (betAmount > UserManager.getDUserFromMessage(message).getBalance()) {
            MessageUtils.sendErrorMessage(channel, "You don't have that much money to bet!");
            return;
        }

        GameManager.createSinglePlayerGame(GameBlackjack.class, channel, message.getAuthorAsMember().block(), betAmount);
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "[bet]", "Play a game of Blackjack and wager money.");
    }

}
