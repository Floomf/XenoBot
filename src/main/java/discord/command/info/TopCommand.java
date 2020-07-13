package discord.command.info;

import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.data.UserManager;
import discord.data.object.user.DUser;
import discord.util.BotUtils;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import discord.util.DiscordColor;
import discord.util.MessageUtils;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.TextChannel;

public class TopCommand extends AbstractCommand {

    public TopCommand() {
        super(new String[]{"top", "leaderboard", "rankings"}, 1, CommandCategory.INFO);
    }

    @Override
    public void execute(Message message, TextChannel channel, String[] args) {
        final int amount = 10;
        String type = args[0].toLowerCase();
        if (!type.matches("xp|bal|balance")) {
            MessageUtils.sendUsageMessage(channel, getUsage(super.getName()));
            return;
        }

        /*
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
        }*/

        List<DUser> users = new ArrayList<>(UserManager.getDUsers());
        users.removeIf(user -> user.asGuildMember() == null);

        if (type.equals("xp")) {
            users.sort((DUser user1, DUser user2) -> user2.getProg().getTotalXP() - user1.getProg().getTotalXP());
        } else {
            users.sort((DUser user1, DUser user2) -> user2.getBalance() - user1.getBalance());
        }

        StringBuilder desc = new StringBuilder();
        for (int i = 0; i < amount; i++) {
            DUser user = users.get(i);
            if (type.equals("xp")) {
                desc.append(String.format("**%d)** %s Lvl **%,d** ― **%,d** XP\n",
                        i + 1,
                        user.asGuildMember().getMention(),
                        user.getProg().getTotalLevel(),
                        user.getProg().getTotalXP()));
            } else {
                desc.append(String.format("**%d)** %s ― **$%,d**\n",
                        i + 1,
                        user.asGuildMember().getMention(),
                        user.getBalance()));
            }
        }

        if (type.equals("xp")) {
            int totalXP = 0;
            for (DUser user : users) {
                totalXP += user.getProg().getTotalXP();
            }
            int finalTotalXP = totalXP; //peepoo
            channel.createEmbed(MessageUtils.getEmbed("Top " + amount + " Progressed Users 📈", desc.toString(), DiscordColor.CYAN.getColor())
                    .andThen(embed -> embed.setFooter(String.format("%,d", finalTotalXP) + " XP has been earned on this guild.", ""))).block();
        } else {
            channel.createEmbed(MessageUtils.getEmbed("Top " + amount + " Richest Users 💰", desc.toString(), DiscordColor.CYAN.getColor())).block();
        }
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "[xp/balance]", "View the top users on this guild, sorted by total XP or current balance.");
    }

}
