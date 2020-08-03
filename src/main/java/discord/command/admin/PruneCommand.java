package discord.command.admin;

import discord4j.core.object.entity.channel.TextChannel;
import reactor.core.publisher.Flux;
import discord.util.BotUtils;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.util.MessageUtils;
import discord4j.core.object.entity.Message;

public class PruneCommand extends AbstractCommand {

    public PruneCommand() {
        super(new String[]{"prune"}, 2, CommandCategory.ADMIN);
    }

    @Override
    public void execute(Message message, TextChannel channel, String[] args) {
        if (!args[0].matches("\\d+")) { //not digits   
            MessageUtils.sendErrorMessage(channel, "Couldn't parse a valid amount of messages to prune.");
            return;
        }

        int amount = Integer.parseInt(args[0]);
        if (amount > 100 || amount < 1) {
            MessageUtils.sendErrorMessage(channel, "Invalid amount of messages to prune! You may prune up to 100 at a time.");
            return;
        }
        TextChannel pruneChannel = BotUtils.getGuildTextChannel(args[1].toLowerCase(), channel.getGuild().block());

        pruneChannel.bulkDelete(pruneChannel.getMessagesBefore(pruneChannel.getLastMessageId().get()).take(amount)
                .map(Message::getId))
                .concatWith(Flux.just(pruneChannel.getLastMessageId().get()))
                .collectList().block();
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "[amount] [channel name]",
                "Mass prune (delete) messages in a channel.\nYou may prune up to 100 messages at a time.");
    }

}
    
