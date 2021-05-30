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
import discord4j.rest.util.ApplicationCommandOptionType;

public class NickCommand extends AbstractCommand {

    public static final int LEVEL_REQUIRED = 30;

    public NickCommand() {
        super(new String[]{"name"}, 1, CommandCategory.PERK);
    }

    @Override
    public ApplicationCommandRequest buildSlashCommand() {
        return ApplicationCommandRequest.builder()
                .name("name")
                .description("Change your nickname on this server")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("new_nick")
                        .description("New nickname")
                        .required(true)
                        .type(ApplicationCommandOptionType.STRING.getValue())
                        .build())
                .build();
    }

    @Override
    public void execute(InteractionContext context) {
        String nick = BotUtils.validateNick(context.getOptionAsString("new_nick"));

        if (nick.isEmpty()) {
            context.replyWithError("Your nickname can only contain basic letters and symbols.");
            return;
        }

        context.getDUser().getName().setNick(nick);
        context.replyWithInfo("Nickname changed. Hi there, " + nick + "!");
        UserManager.saveDatabase();
    }

    @Override
    public void execute(Message message, TextChannel channel, String[] args) {
        String nick = BotUtils.validateNick(CommandHandler.combineArgs(0, args));

        if (nick.isEmpty()) {
            MessageUtils.sendErrorMessage(channel, "Your nickname can only contain basic letters and symbols.");
            return;
        }

        UserManager.getDUserFromMessage(message).getName().setNick(nick);
        MessageUtils.sendInfoMessage(channel, "Nickname updated. Pleasure to meet ya, " + nick + "." +
                "\n\n**__Slash Commands:__**" +
                "\nYou can now type **/name** to change your name instead.");
        UserManager.saveDatabase();
    }

    @Override
    public int getLevelRequired() {
        return LEVEL_REQUIRED;
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "[new name]", "Change your nickname on this guild.");
    }

}
