package discord.command.admin;

import reactor.core.publisher.Flux;
import discord.util.BotUtils;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.util.MessageUtils;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.TextChannel;

public class PruneCommand extends AbstractCommand {

    public PruneCommand() {
        super(new String[]{"prune"}, 1, CommandCategory.ADMIN);
    }

    @Override
    public void execute(Message message, TextChannel channel, String[] args) {
        if (!args[0].matches("\\d+")) { //not digits   
            MessageUtils.sendErrorMessage(channel, "Couldn't parse a valid amount of messages to prune.");
            return;
        }

        int amount = Integer.parseInt(args[0]);
        if (amount > 50 || amount < 1) {
            MessageUtils.sendErrorMessage(channel, "Invalid amount of messages to prune! You may prune up to 50 at a time.");
            return;
        }
        channel.bulkDelete(channel.getMessagesBefore(message.getId()).take(amount)
                .concatWith(Flux.just(message))
                .map(Message::getId))
                .collectList().block();
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "[amount]",
                "Mass prune (delete) messages in this channel.\nYou may prune up to 50 messages at a time.");
    }

}
    
