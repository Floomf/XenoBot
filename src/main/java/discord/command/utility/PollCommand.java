package discord.command.utility;

import discord.util.BotUtils;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;

import java.awt.Color;

import discord.util.DiscordColor;
import discord.util.MessageUtils;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.object.entity.Message;

public class PollCommand extends AbstractCommand {

    private final static String[] EMOJI_LETTERS = {"🇦", "🇧", "🇨", "🇩", "🇪", "🇫", "🇬", "🇭", "🇮", "🇯",
            "🇰", "🇱", "🇲", "🇳", "🇴", "🇵", "🇶", "🇷", "🇸", "🇹"};

    public PollCommand() {
        super(new String[]{"poll"}, 2, CommandCategory.UTILITY);
    }

    @Override
    public void execute(Message message, TextChannel channel, String[] args) {
        StringBuilder sb = new StringBuilder();
        int options = args.length - 1;
        if (options > EMOJI_LETTERS.length) {
            options = EMOJI_LETTERS.length;
        }
        for (int i = 0; i < options; i++) {
            sb.append(EMOJI_LETTERS[i]).append("  ").append(args[i + 1]).append("\n");
        }
        TextChannel pollChannel = BotUtils.getGuildTextChannel("general", message.getGuild().block());
        Message pollMessage = pollChannel.createEmbed(MessageUtils.getEmbed(args[0], sb.toString(), DiscordColor.ORANGE.getColor())
                .andThen(embed -> embed.setFooter("(Poll by " + message.getAuthorAsMember().block().getDisplayName() + ")", ""))).block();

        for (int i = 0; i < options; i++) {
            pollMessage.addReaction(ReactionEmoji.unicode(EMOJI_LETTERS[i])).block();
        }
        MessageUtils.sendInfoMessage(channel, "Poll created in " + pollChannel.getMention() + " chat.");
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "[title] [options]",
                "Create a custom poll.\n\nTo include multiple words in an argument, "
                        + "you must wrap it in quotations."
                        + "\n*Example:* `!poll \"Candy corn?\" \"Yes, of course!\" No`");
    }

}
