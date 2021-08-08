package discord.command.admin;

import discord.util.BotUtils;
import discord.manager.UserManager;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.util.MessageUtils;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.TextChannel;

import java.util.List;

public class XPCommand extends AbstractCommand {

    public XPCommand() {
        super( "gxp", 2, CommandCategory.ADMIN);
    }

    @Override
    public void execute(Message message, TextChannel channel, String[] args) {
        User[] mentions = message.getUserMentions().stream().filter(user -> !user.isBot()).toArray(User[]::new);

        if (mentions.length == 0) {
            MessageUtils.sendErrorMessage(channel, "Couldn't identify any user. Please @mention at least one.");
            return;
        }

        //check xp amount
        double xp;
        try {
            xp = Double.parseDouble(args[0]);
            if (xp > 20000.0) xp = 20000.0;
        } catch (NumberFormatException ex) {
            MessageUtils.sendErrorMessage(channel, "Couldn't parse the specified XP amount.");
            return;
        }

        for (User user : mentions) {
            UserManager.getDUserFromID(user.getId().asLong()).getProg().addXP(xp);
            MessageUtils.sendInfoMessage(channel, "Gave " + user.getUsername() + " **" + xp + "**XP");
        }
        UserManager.saveDatabase();
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "[amount] [@mentions]",
                "Give users XP in the database.");
    }

}
