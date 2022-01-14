package discord.command.perk;

import discord.core.command.InteractionContext;
import discord.util.BotUtils;
import discord.core.command.CommandHandler;
import discord.manager.UserManager;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.util.MessageUtils;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.core.object.command.ApplicationCommandOption;

public class DescCommand extends AbstractCommand {

    public DescCommand() {
        super("description", 1, CommandCategory.PERK);
    }

    @Override
    public ApplicationCommandRequest buildSlashCommand() {
        return ApplicationCommandRequest.builder()
                .name(getName())
                .description("Change your description on your profile")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("new_desc")
                        .description("New description")
                        .required(true)
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .build())
                .build();
    }

    @Override
    public void execute(InteractionContext context) {
        String desc = BotUtils.validateString(context.getOptionAsString("new_desc"));

        if (desc.isEmpty()) {
            context.replyWithError("Couldn't parse a valid description. Only basic characters are allowed.");
            return;
        } else if (desc.length() > 150) {
            desc = desc.substring(0, 150);
        }

        context.getDUser().setDesc(desc);
        UserManager.saveDatabase();
        context.replyWithInfo("How expressive! Updated your description accordingly.");
    }

    @Override
    public void execute(Message message, TextChannel channel, String[] args) {
        String desc = BotUtils.validateString(CommandHandler.combineArgs(0, args));

        if (desc.isEmpty()) {
            MessageUtils.sendErrorMessage(channel, "Couldn't parse a valid description. Only basic characters are allowed.");
            return;
        } else if (desc.equalsIgnoreCase("clear")) {
            desc = "";
        } else if (desc.length() > 150) {
            desc = desc.substring(0, 150);
        }

        UserManager.getDUserFromMessage(message).setDesc(desc);
        UserManager.saveDatabase();
        MessageUtils.sendInfoMessage(channel, "How expressive! Updated your description accordingly.");
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "[text]", "Change your description on your profile."
                + "\n\n**Special Arguments**"
                + "\n`!" + alias + " clear` - Clear your current description.");
    }

}
