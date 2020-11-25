package discord.command.admin;

import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.core.command.CommandHandler;
import discord.util.BotUtils;
import discord.util.MessageUtils;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;

public class SudoCommand extends AbstractCommand {

        public SudoCommand() {
            super(new String[]{"sudo"}, 1, CommandCategory.ADMIN);
        }

        @Override
        public void execute(Message message, TextChannel channel, String[] args) {
            Snowflake messageId;
            try {
                messageId = Snowflake.of(Long.parseLong(args[0]));
            } catch (NumberFormatException e) {
                MessageUtils.sendErrorMessage(channel, "Couldn't parse a valid message ID to use.");
                return;
            }
            channel.getMessageById(messageId)
                    .doOnError(e -> MessageUtils.sendErrorMessage(channel, "Couldn't get a message by that ID"))
                    .onErrorStop()
                    .doOnNext(CommandHandler::processCommand)
                    .block();
        }

        @Override
        public String getUsage(String alias) {
            return BotUtils.buildUsage(alias, "[messageID]", "Attempt to run a command from a specified message by its author.");
        }

}
