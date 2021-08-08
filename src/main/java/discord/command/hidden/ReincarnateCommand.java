package discord.command.hidden;

import discord.core.command.InteractionContext;
import discord.util.BotUtils;
import discord.core.command.CommandHandler;
import discord.manager.UserManager;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.data.object.user.DUser;

import discord.util.DiscordColor;
import discord.util.MessageUtils;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.discordjson.json.ApplicationCommandRequest;

public class ReincarnateCommand extends AbstractCommand {

    public ReincarnateCommand() {
        super("reincarnate", 0, CommandCategory.HIDDEN);
    }

    @Override
    public ApplicationCommandRequest buildSlashCommand() {
        return ApplicationCommandRequest.builder()
                .name(getName())
                .description("Reincarnate into a new life, resetting your unlocks/progress, but gain a permanent 50% XP boost")
                .build();
    }

    @Override
    public void execute(InteractionContext context) {
        DUser dUser = context.getDUser();
        if (dUser.getProg().getReincarnation().isMax()) {
            context.reply(MessageUtils.getEmbed( "No more.", "*This is my last life.*"));
            return;
        } else if (!dUser.getProg().getPrestige().isMax()) {
            context.reply(MessageUtils.getEmbed( "Not yet..", "*I still have this life to live.*"));
            return;
        }

        if (dUser.getProg().getReincarnation().isMax()) {
            context.reply(MessageUtils.getEmbed( "A Final Beginning", "You have been reborn into your last life: **"
                    + dUser.getProg().getReincarnation().getRomaji() + "**.", DiscordColor.PINK));
        } else {
            context.reply(MessageUtils.getEmbed( "A New Beginning", "You have been reborn into **"
                    + dUser.getProg().getReincarnation().getRomaji() + "**.", DiscordColor.PINK));
        }
        dUser.getProg().reincarnate();
    }

    @Override
    public void execute(Message message, TextChannel channel, String[] args) {
        DUser user = UserManager.getDUserFromMessage(message);
        if (user.getProg().getReincarnation().isMax()) {
            MessageUtils.sendMessage(channel, "No more.", "*This is my last life.*");
            return;
        } else if (!user.getProg().getPrestige().isMax()) {
            MessageUtils.sendMessage(channel, "Not yet..", "*I still have this life to live.*");
            return;
        }

        if (args.length == 0) {
            MessageUtils.sendInfoMessage(channel, "Are you **absolutely sure** you want to reincarnate? "
                    + "Your progress this life will be completely reset, losing your level, prestige, badges, and all unlocks."
                    + "\n\nIf you are truly ready, confirm your reincarnation by typing `!reincarnate "
                    + user.getName().getNick() + '`');
            return;
        }

        String name = CommandHandler.combineArgs(0, args);
        if (!name.equals(user.getName().getNick())) {
            MessageUtils.sendErrorMessage(channel, "Invalid confirmation. If you are truly ready, "
                    + "confirm your reincarnation by typing `!reincarnate " + user.getName().getNick() + "`");
            return;
        }
        user.getProg().reincarnate();
        if (user.getProg().getReincarnation().isMax()) {
            MessageUtils.sendMessage(channel, "A Final Beginning", "You have been reborn into your last life: **"
                    + user.getProg().getReincarnation().getRomaji() + "**.", DiscordColor.PINK);
        } else {
            MessageUtils.sendMessage(channel, "A New Beginning", "You have been reborn into **"
                    + user.getProg().getReincarnation().getRomaji() + "**.", DiscordColor.PINK);
        }
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "", "Reincarnate into a new life and reset **completely** back to level one, "
                + "losing all unlocks, badges, and your prestige, but gain a permanent XP boost.");
    }


}
