package discord.command.perk;

import discord.core.command.CommandHandler;
import discord.core.command.InteractionContext;
import discord.data.object.ShopItem;
import discord.data.object.user.DUser;
import discord.util.BotUtils;
import discord.manager.UserManager;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.util.MessageUtils;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.discordjson.json.ApplicationCommandInteractionOptionData;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.util.ApplicationCommandOptionType;

public class EmojiCommand extends AbstractCommand {

    public static final int LEVEL_REQUIRED = 50;

    public EmojiCommand() {
        super(new String[]{"emoji"}, 1, CommandCategory.PERK);
    }

    @Override
    public ApplicationCommandRequest buildSlashCommand() {
        return ApplicationCommandRequest.builder()
                .name("emoji")
                .description("Set emoji(s) next to your name")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("set")
                        .description("Set emoji(s) next to your name")
                        .type(ApplicationCommandOptionType.SUB_COMMAND.getValue())
                        .addOption(ApplicationCommandOptionData.builder()
                                .name("emojis")
                                .description("Emoji(s) to set")
                                .type(ApplicationCommandOptionType.STRING.getValue())
                                .required(true)
                                .build())
                        .build())
                .addOption(ApplicationCommandOptionData.builder()
                        .name("clear")
                        .description("Clear any name emojis you currently have")
                        .type(ApplicationCommandOptionType.SUB_COMMAND.getValue())
                        .build())
                .build();
    }

    @Override
    public void execute(InteractionContext context) {
        ApplicationCommandInteractionOption sub = context.getSubCommand();

        DUser dUser = context.getDUser();
        if (sub.getName().equals("clear")) {
            dUser.getName().setEmojis("");
            context.replyWithInfo("Any name emojis have been cleared.");
            UserManager.saveDatabase();
        } else {
            String emojis = sub.getOption("emojis").get().getValue().get().asString();
            int limit = dUser.hasPurchased(ShopItem.EXTENDED_EMOJIS) ? 12 : 6;

            if (emojis.length() > limit) {
                emojis = emojis.substring(0, limit);
            }

            if (emojis.isEmpty()) {
                context.replyWithError("Couldn't parse any emojis/symbols from your input.");
            } else {
                dUser.getName().setEmojis(emojis);
                context.replyWithInfo("Splendid choice! Your name has been updated.");
                UserManager.saveDatabase();
            }
        }
    }

    @Override
    public void execute(Message message, TextChannel channel, String[] args) {
        DUser dUser = UserManager.getDUserFromMessage(message);
        String emojis = CommandHandler.combineArgs(0, args).replace(" ", "");
        if (emojis.toLowerCase().equals("none")) {
            dUser.getName().setEmojis("");
            MessageUtils.sendInfoMessage(channel, "Your name emojis/symbols have been removed.");
            UserManager.saveDatabase();
        } else {
            int limit = dUser.hasPurchased(ShopItem.EXTENDED_EMOJIS) ? 12 : 6;

            if (emojis.length() > limit) {
                emojis = emojis.substring(0, limit);
            }

            if (emojis.isEmpty()) {
                MessageUtils.sendErrorMessage(channel, "Couldn't parse any emojis/symbols from your input.");
            } else {
                dUser.getName().setEmojis(emojis);
                MessageUtils.sendInfoMessage(channel, "Splendid choice! Updated your name accordingly.");
                UserManager.saveDatabase();
            }
        }
    }

    @Override
    public int getLevelRequired() {
        return LEVEL_REQUIRED;
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "[emojis/symbols]",
                "Set 3-6 emojis/symbols next to your name (String length <= 6).\nYou can extend this to 6-12 emojis/symbols through the `!shop`."
                + "\n\n**Special Argument**"
                + "\n`!" + alias + " none` - Remove your current emoji(s).");
    }

}
