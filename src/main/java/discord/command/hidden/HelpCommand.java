package discord.command.hidden;

import discord.core.command.InteractionContext;
import discord.listener.EventsHandler;
import discord.util.BotUtils;
import discord.core.command.CommandManager;
import discord.manager.UserManager;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;

import java.util.HashSet;

import discord.util.DiscordColor;
import discord.util.MessageUtils;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.discordjson.json.ApplicationCommandRequest;

public class HelpCommand extends AbstractCommand {

    public HelpCommand() {
        super("help", 0, CommandCategory.HIDDEN);
    }

    @Override
    public ApplicationCommandRequest buildSlashCommand() {
        return ApplicationCommandRequest.builder()
                .name(getName())
                .description("View all available commands")
                .build();
    }

    @Override
    public void execute(InteractionContext context) {
        HashSet<AbstractCommand> commands = CommandManager.getAllCommands();
        commands.removeIf(command -> (!command.isSupportedGlobally() && !context.getGuild().getId().equals(EventsHandler.THE_REALM_ID)
                && command.buildOutsideGuildSlashCommand() == null)
                || command.buildSlashCommand() == null
                || command.getCategory() == CommandCategory.HIDDEN
                || (context.getDUser() != null)
                && command.getLevelRequired() > context.getDUser().getProg().getTotalLevelThisLife());

        context.reply(MessageUtils.getEmbed("Available Commands", "", DiscordColor.PURPLE)
                .andThen(embed -> {
                    for (CommandCategory c : CommandCategory.values()) {
                        StringBuilder sb = new StringBuilder();
                        commands.stream().filter(cmd -> cmd.getCategory() == c)
                                .forEach(cmd -> sb.append("`/").append(cmd.getName()).append("`  "));
                        if (sb.length() > 0) { //skip over empty categories
                            embed.addField(c.toString(), sb.toString(), false);
                        }
                    }
                }));
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
            commands.removeIf(command -> !command.isSupportedGlobally() && !channel.getGuildId().equals(EventsHandler.THE_REALM_ID)
                    || command.getCategory() == CommandCategory.HIDDEN //Take out hidden
                    || (command.getCategory() == CommandCategory.PERK //take out not unlocked
                    && command.getLevelRequired() > UserManager.getDUserFromMessage(message).getProg().getTotalLevelThisLife())
                    || (command.getCategory() == CommandCategory.ADMIN //take out admin if not owner
                    && !message.getAuthorAsMember().block().equals(message.getGuild().block().getOwner().block())));

            channel.createMessage(spec -> {
                spec.addEmbed(MessageUtils.getEmbed("Available Commands", "*For info on a command, use* **`!help [command]`**",
                        DiscordColor.PURPLE)
                        .andThen(embed -> {
                            for (CommandCategory c : CommandCategory.values()) {
                                StringBuilder sb = new StringBuilder();
                                commands.stream().filter(cmd -> cmd.getCategory() == c)
                                        .forEach(cmd -> {
                                            if (cmd.buildSlashCommand() != null) {
                                                sb.append("`/");
                                            } else {
                                                sb.append("`!");
                                            }
                                            sb.append(cmd.getName()).append("`  ");
                                        });
                                if (sb.length() > 0) { //skip over empty categories
                                    embed.addField(c.toString(), sb.toString(), false);
                                }
                            }
                        }));
            }).block();

        }
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "", "View available commands.");
    }

    @Override
    public boolean isSupportedGlobally() {
        return true;
    }

}
