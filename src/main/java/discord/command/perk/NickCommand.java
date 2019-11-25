package discord.command.perk;

import discord.util.BotUtils;
import discord.core.command.CommandHandler;
import discord.data.UserManager;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.util.MessageUtils;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.TextChannel;

public class NickCommand extends AbstractCommand {

    public static final int LEVEL_REQUIRED = 40;

    public NickCommand() {
        super(new String[]{"nick", "name", "nickname"}, 1, CommandCategory.PERK);
    }

    @Override
    public void execute(Message message, TextChannel channel, String[] args) {
        String nick = BotUtils.validateNick(CommandHandler.combineArgs(0, args));

        if (nick.isEmpty()) {
            MessageUtils.sendErrorMessage(channel, "Your nickname can only contain basic letters and symbols.");
            return;
        }

        UserManager.getDUserFromMessage(message).getName().setNick(nick);
        MessageUtils.sendInfoMessage(channel, "Nickname updated. Pleasure to meet ya, " + nick + ".");
        UserManager.saveDatabase();
    }

    @Override
    public int getLevelRequired() {
        return LEVEL_REQUIRED;
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "[new name]", "Change your nickname on this guild.");
    }

}
