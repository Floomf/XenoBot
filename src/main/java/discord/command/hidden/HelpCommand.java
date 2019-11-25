package discord.command.hidden;

import discord.util.BotUtils;
import discord.core.command.CommandManager;
import discord.data.UserManager;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;

import java.awt.*;
import java.util.HashSet;

import discord.util.MessageUtils;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.entity.Message;

public class HelpCommand extends AbstractCommand {

    public HelpCommand() {
        super(new String[]{"help", "commands"}, 0, CommandCategory.HIDDEN);
    }

    @Override
    public void execute(Message message, TextChannel channel, String[] args) {
        if (args.length > 0) {
            String name = args[0].toLowerCase();
            AbstractCommand cmd = CommandManager.getCommand(name);
            if (cmd == null) {
                MessageUtils.sendErrorMessage(channel, "Couldn't find a command by that name. "
                        + "Use `!help` for a list of available commands.");
            } else {
                MessageUtils.sendUsageMessage(channel, cmd.getUsage(name));
            }
        } else {
            HashSet<AbstractCommand> commands = CommandManager.getAllCommands();
            commands.removeIf(command -> command.getCategory() == CommandCategory.HIDDEN //Take out hidden
                    || (command.getCategory() == CommandCategory.PERK //take out not unlocked
                    && command.getLevelRequired() > UserManager.getDUserFromMessage(message).getProg().getTotalLevelThisLife())
                    || (command.getCategory() == CommandCategory.ADMIN //take out admin if not owner
                    && !message.getAuthorAsMember().block().equals(message.getGuild().block().getOwner().block())));

            channel.createMessage(spec -> spec.setEmbed(MessageUtils.message("Available Commands",
                    "*For info on a command, use* **`!help [command]`**", Color.CYAN)
                    .andThen(embed -> {
                        for (CommandCategory c : CommandCategory.values()) {
                            StringBuilder sb = new StringBuilder();
                            commands.stream().filter(cmd -> cmd.getCategory() == c)
                                    .forEach(cmd -> sb.append("`!").append(cmd.getName()).append("`  "));
                            if (sb.length() > 0) { //skip over empty categories
                                embed.addField(c.toString(), sb.toString(), false);
                            }
                        }
                    }))).block();

        }
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "", "View available commands.");
    }

}
