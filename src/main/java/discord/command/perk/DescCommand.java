package discord.command.perk;

import discord.util.BotUtils;
import discord.core.command.CommandHandler;
import discord.data.UserManager;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.util.MessageUtils;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.TextChannel;

public class DescCommand extends AbstractCommand {

    public static final int LEVEL_REQUIRED = 20;

    public DescCommand() {
        super(new String[]{"desc", "motto", "title"}, 1, CommandCategory.PERK);
    }

    @Override
    public void execute(Message message, TextChannel channel, String[] args) {
        String desc = BotUtils.validateString(CommandHandler.combineArgs(0, args));

        if (desc.isEmpty()) {
            MessageUtils.sendErrorMessage(channel, "Couldn't parse a valid description. Only basic characters are allowed.");
            return;
        } else if (desc.length() > 150) {
            desc = desc.substring(0, 150);
        }

        UserManager.getDUserFromMessage(message).setDesc(desc);
        UserManager.saveDatabase();
        MessageUtils.sendInfoMessage(channel, "How expressive! Updated your description accordingly.");
    }

    @Override
    public int getLevelRequired() {
        return LEVEL_REQUIRED;
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "[text]", "Set a description on your profile.");
    }

}
