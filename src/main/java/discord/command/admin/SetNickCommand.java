package discord.command.admin;

import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.TextChannel;
import discord.util.BotUtils;
import discord.core.command.CommandHandler;
import discord.manager.UserManager;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.data.object.user.DUser;

import discord.util.MessageUtils;
import discord4j.core.object.entity.Message;

public class SetNickCommand extends AbstractCommand {

    public SetNickCommand() {
        super(new String[]{"setnick", "changenick", "setname", "sn"}, 2, CommandCategory.ADMIN);
    }

    @Override
    public void execute(Message message, TextChannel channel, String[] args) {
        User mention = message.getUserMentions().filter(user -> !user.isBot()).blockFirst();
        if (mention == null) {
            MessageUtils.sendErrorMessage(channel, "Couldn't parse a user. Please @mention them.");
            return;
        }
        DUser userToChange = UserManager.getDUserFromUser(mention);
        if (userToChange == null) {
            MessageUtils.sendErrorMessage(channel, "Couldn't find that user in the database. Are they a bot?");
            return;
        }
        String nick = BotUtils.validateNick(CommandHandler.combineArgs(1, args));
        if (nick.isEmpty()) {
            MessageUtils.sendErrorMessage(channel, "The nickname can only contain basic letters and symbols.");
            return;
        }

        userToChange.getName().setNick(nick);
        MessageUtils.sendInfoMessage(channel, "Nickname updated to " + nick);
        UserManager.saveDatabase();
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "[@mention] [new nickname]",
                "Change the nickname of a user on this guild.");
    }

}
