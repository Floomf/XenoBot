package discord.command.hidden;

import discord.core.command.InteractionContext;
import discord.util.BotUtils;
import discord.manager.UserManager;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.data.object.user.Progress;
import discord.util.MessageUtils;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.discordjson.json.ApplicationCommandRequest;

public class PrestigeCommand extends AbstractCommand {

    public PrestigeCommand() {
        super(new String[]{"prestige"}, 0, CommandCategory.HIDDEN);
    }

    @Override
    public ApplicationCommandRequest buildSlashCommand() {
        return ApplicationCommandRequest.builder()
                .name("prestige")
                .description("Prestige and reset back to level one")
                .build();
    }

    @Override
    public void execute(InteractionContext context) {
        Progress prog = context.getDUser().getProg();
        if (prog.getPrestige().isMax()) {
            context.replyWithError( "You have already reached the maximum prestige.");
        } else if (prog.isNotMaxLevel()) {
            context.replyWithError( "You must be max level (**" + Progress.MAX_LEVEL + "**) to prestige."
                    + " You can type `/profile` to view your progress.");
        } else { //Is max level
            prog.prestige();
            context.reply(MessageUtils.getEmbed("Movin' on up", "Promoted to Prestige "
                    + prog.getPrestige().getNumber()
                    + (prog.getReincarnation().isReincarnated() ? ", *again?*" : "."))); //handle reincarnated
        }
    }

    @Override
    public void execute(Message message, TextChannel channel, String[] args) {
        Progress prog = UserManager.getDUserFromMessage(message).getProg();
        if (prog.getPrestige().isMax()) {
            MessageUtils.sendInfoMessage(channel, "You have already reached the maximum prestige.");
        } else if (prog.isNotMaxLevel()) {
            MessageUtils.sendErrorMessage(channel, "You must be max level (**"
                    + Progress.MAX_LEVEL + "**) to prestige."
                    + " You can use `!prog` to view your progress.");
        } else { //Is max level
            prog.prestige();
            MessageUtils.sendMessage(channel, "Movin' on up", "Promoted to Prestige "
                    + prog.getPrestige().getNumber()
                    + (prog.getReincarnation().isReincarnated() ? ", *again?*" : ".")); //handle reincarnated
        }
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "", "Prestige and carry over back to level one."
                + "\n*(Level " + Progress.MAX_LEVEL + ")*");
    }
}
