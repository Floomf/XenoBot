package discord.command.perk;

import discord.core.command.CommandHandler;
import discord.data.object.ShopItem;
import discord.data.object.user.DUser;
import discord.util.BotUtils;
import discord.manager.UserManager;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.util.MessageUtils;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;

public class EmojiCommand extends AbstractCommand {

    public static final int LEVEL_REQUIRED = 60;

    public EmojiCommand() {
        super(new String[]{"emoji", "emojis", "symbol", "symbols"}, 1, CommandCategory.PERK);
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
