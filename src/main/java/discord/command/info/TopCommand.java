package discord.command.info;

import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.command.game.hangman.GameHangman;
import discord.command.game.math.GameMath;
import discord.core.command.InteractionContext;
import discord.manager.UserManager;
import discord.data.object.user.DUser;
import discord.util.BotUtils;

import java.util.ArrayList;
import java.util.List;

import discord.util.DiscordColor;
import discord.util.MessageUtils;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.util.ApplicationCommandOptionType;

public class TopCommand extends AbstractCommand {

    public TopCommand() {
        super("top", 1, CommandCategory.INFO);
    }

    @Override
    public ApplicationCommandRequest buildSlashCommand() {
        return ApplicationCommandRequest.builder()
                .name(getName())
                .description("View the top users")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("xp")
                        .description("View the top users by XP earned")
                        .type(ApplicationCommandOptionType.SUB_COMMAND.getValue())
                        .build())
                .addOption(ApplicationCommandOptionData.builder()
                        .name("balance")
                        .description("View the top users by money earned")
                        .type(ApplicationCommandOptionType.SUB_COMMAND.getValue())
                        .build())
                .addOption(ApplicationCommandOptionData.builder()
                        .name("math")
                        .description("View the leaderboard for Quick Math")
                        .type(ApplicationCommandOptionType.SUB_COMMAND.getValue())
                        .build())
                .addOption(ApplicationCommandOptionData.builder()
                        .name("hangman")
                        .description("View the leaderboard for Hangman")
                        .type(ApplicationCommandOptionType.SUB_COMMAND.getValue())
                        .build())
                .build();
    }

    @Override
    public ApplicationCommandRequest buildOutsideGuildSlashCommand() {
        return ApplicationCommandRequest.builder()
                .name("top")
                .description("View the top users")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("math")
                        .description("View the leaderboard for Quick Math")
                        .type(ApplicationCommandOptionType.SUB_COMMAND.getValue())
                        .build())
                .addOption(ApplicationCommandOptionData.builder()
                        .name("hangman")
                        .description("View the leaderboard for Hangman")
                        .type(ApplicationCommandOptionType.SUB_COMMAND.getValue())
                        .build())
                .build();
    }

    @Override
    public void execute(InteractionContext context) {
        final int amount = 15;
        String type = context.getSubCommand().getName();

        if (type.equals("math")) {
            context.reply(MessageUtils.getEmbed("Quick Math High Scores", GameMath.LEADERBOARD.toString(), DiscordColor.ORANGE));
            return;
        } else if (type.equals("hangman")) {
            context.reply(MessageUtils.getEmbed("Hangman Longest Win Streaks", GameHangman.LEADERBOARD.toString(), DiscordColor.ORANGE));
            return;
        }

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
            context.reply(MessageUtils.getEmbed("Top " + amount + " Progressed Users 📈", desc.toString(), DiscordColor.CYAN)
                    .andThen(embed -> embed.setFooter(String.format("%,d", finalTotalXP) + " XP has been earned on this guild.", "")));
        } else {
            context.reply(MessageUtils.getEmbed("Top " + amount + " Richest Users 💰", desc.toString(), DiscordColor.CYAN));
        }
    }

    @Override
    public void execute(Message message, TextChannel channel, String[] args) {
        final int amount = 15;
        String type = args[0].toLowerCase();
        if (!type.matches("exp|xp|bal|balance")) {
            MessageUtils.sendUsageMessage(channel, getUsage(super.getName()));
            return;
        }

        List<DUser> users = new ArrayList<>(UserManager.getDUsers());
        users.removeIf(user -> user.asGuildMember() == null);

        if (type.equals("xp") || type.equals("exp")) {
            users.sort((DUser user1, DUser user2) -> user2.getProg().getTotalXP() - user1.getProg().getTotalXP());
        } else {
            users.sort((DUser user1, DUser user2) -> user2.getBalance() - user1.getBalance());
        }

        StringBuilder desc = new StringBuilder();
        for (int i = 0; i < amount; i++) {
            DUser user = users.get(i);
            if (type.equals("xp") || type.equals("exp")) {
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

        if (type.equals("xp") || type.equals("exp")) {
            int totalXP = 0;
            for (DUser user : users) {
                totalXP += user.getProg().getTotalXP();
            }
            int finalTotalXP = totalXP; //peepoo
            channel.createMessage(spec -> {
                spec.addEmbed(MessageUtils.getEmbed("Top " + amount + " Progressed Users 📈", desc.toString(), DiscordColor.CYAN)
                        .andThen(embed -> embed.setFooter(String.format("%,d", finalTotalXP) + " XP has been earned on this guild.", "")));
            }).block();
        } else {
            channel.createMessage(spec -> {
                spec.addEmbed(MessageUtils.getEmbed("Top " + amount + " Richest Users 💰", desc.toString(), DiscordColor.CYAN));
            }).block();
        }
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "[xp/balance]", "View the top users on this guild, sorted by total XP or current balance.");
    }

}
