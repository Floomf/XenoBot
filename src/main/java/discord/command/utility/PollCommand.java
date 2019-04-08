package discord.command.utility;

import discord.util.BotUtils;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import java.awt.Color;
import sx.blah.discord.handle.impl.obj.ReactionEmoji;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.RequestBuffer;

public class PollCommand extends AbstractCommand {
    
    private final static String[] EMOJI_LETTERS = {"🇦", "🇧", "🇨", "🇩", "🇪", "🇫", "🇬", "🇭", "🇮", "🇯", "🇰", "🇱", "🇲", "🇳", "🇴"}; 
    
    public PollCommand() {
        super(new String[] {"poll"}, 2, CommandCategory.UTILITY);
    }
    
    @Override
    public void execute(IMessage message, String[] args) {     
        StringBuilder sb = new StringBuilder();
        int options = args.length - 1;
        if (options > EMOJI_LETTERS.length) {
            options = EMOJI_LETTERS.length;
        }
        for (int i = 0; i < options; i++) {
            sb.append(EMOJI_LETTERS[i]).append("  ").append(args[i + 1]).append("\n");
        }
        EmbedBuilder builder = BotUtils.getBuilder(message.getClient(), args[0], sb.toString(), Color.ORANGE);
        builder.withFooterText("Poll created by " + message.getAuthor().getDisplayName(message.getGuild()));
        IChannel pollChannel = message.getGuild().getChannelsByName("general").get(0);
        IMessage pollMessage = pollChannel.sendMessage(builder.build());
        for (int i = 0; i < options; i++) {
            final int j = i;
            RequestBuffer.request(() -> pollMessage.addReaction(ReactionEmoji.of(EMOJI_LETTERS[j]))).get();
        }
        BotUtils.sendInfoMessage(message.getChannel(), "Poll created in <#" + pollChannel.getLongID() + "> chat.");
    }
    
    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "[title] [options]", 
                "Create a custom poll.\n\nTo include multiple words in an argument, "
                + "you can wrap it in quotations."
                + "\n*Example:* `!poll \"Candy corn?\" \"Yes, of course\" No`");
    }
    
}
