package discord.command.info;

import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.data.UserManager;
import discord.data.object.user.DUser;
import discord.util.BotUtils;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import discord.util.MessageUtils;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.TextChannel;

public class TopCommand extends AbstractCommand {

    public TopCommand() {
        super(new String[]{"top", "leaderboard", "rankings"}, 0, CommandCategory.INFO);
    }

    @Override
    public void execute(Message message, TextChannel channel, String[] args) {
        if (args.length > 0 && args[0].matches("\\D+")) {
            MessageUtils.sendErrorMessage(channel, "Couldn't parse a valid amount of users to display.");
            return;
        }

        int amount = 10;
        if (args.length > 0) {
            amount = Integer.parseInt(args[0]);
            if (amount > 25) {
                amount = 25;
            } else if (amount < 1) {
                MessageUtils.sendErrorMessage(channel, "Please specify a valid amount of users (1-25) to display.");
                return;
            }
        }

        List<DUser> users = new ArrayList<>(UserManager.getDUsers());
        users.removeIf(user -> user.asGuildMember() == null);
        users.sort((DUser user1, DUser user2) -> user1.getProg().getTotalXP() - user2.getProg().getTotalXP());
        Collections.reverse(users);
        StringBuilder desc = new StringBuilder();
        for (int i = 0; i < amount; i++) {
            DUser user = users.get(i);
            desc.append(String.format("**%d)** %s `Lvl `**`%,d`**`  -  `**`%,d`**` XP`\n",
                    i + 1,
                    user.asGuildMember().getMention(),
                    user.getProg().getTotalLevel(),
                    user.getProg().getTotalXP()));
        }

        int totalXP = 0;
        for (DUser user : users) {
            totalXP += user.getProg().getTotalXP();
        }

        int finalAmount = amount; //poooopeee
        int finalTotalXP = totalXP; //peepoo
        channel.createMessage(spec -> spec.setEmbed(MessageUtils.message("Top " + finalAmount + " Users", desc.toString(), Color.CYAN)
                .andThen(embed -> embed.setFooter(String.format("%,d", finalTotalXP) + " XP has been earned on this guild.", "")))).block();
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "", "View the top users progressed on this guild."
                + "\n\nOptionally, you can specify a number (1-25) of users to display as an argument.");
    }

}
