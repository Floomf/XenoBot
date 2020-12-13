package discord.command.admin;

import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.manager.UserManager;
import discord.util.BotUtils;
import discord.util.MessageUtils;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.TextChannel;

public class BirthdayCommand extends AbstractCommand {

    public BirthdayCommand() {
        super(new String[]{"birthday", "bday"}, 4, CommandCategory.ADMIN);
    }

    @Override
    public void execute(Message message, TextChannel channel, String[] args) {
        User mention = message.getUserMentions().filter(user -> !user.isBot()).blockFirst();

        if (mention == null) {
            MessageUtils.sendErrorMessage(channel, "Couldn't identify any user. Please @mention them.");
            return;
        }

        int day, month, year;

        try {
            day = Integer.parseInt(args[1]);
            month = Integer.parseInt(args[2]);
            year = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            MessageUtils.sendErrorMessage(channel, "Couldn't parse a valid date. Please use numbers only.");
            return;
        }

        UserManager.getDUserFromUser(mention).setBirthday(day, month, year);
        UserManager.saveDatabase();
        MessageUtils.sendInfoMessage(channel, "Birthday set to **" + day + "/" + month + "/" + year + "**.");
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "@mention (day) (month) (year)", "Set the birthday of a user.");
    }
}
