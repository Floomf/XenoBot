package discord.command.hidden;

import discord.BotUtils;
import discord.CommandManager;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import java.util.HashSet;
import java.util.stream.Stream;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

public class HelpCommand extends AbstractCommand {
    
    public HelpCommand() {
        super(new String[] {"help", "commands"}, 0, CommandCategory.HIDDEN);
    }
    
    public void execute(IMessage message, String[] args) {
        IChannel channel = message.getChannel();
        if (args.length > 0) {
            String name = args[0].toLowerCase();
            AbstractCommand cmd = CommandManager.getCommand(name);
            if (cmd == null) {
                BotUtils.sendErrorMessage(channel, "Could not find a command by that name.");
            } else {
                BotUtils.sendUsageMessage(channel, cmd.getUsage(name));
            }
        } else {
            EmbedBuilder builder = BotUtils.getBuilder("Available Commands", 
                    "*For info on a command, use:* **!help [command]**");
            
            HashSet<AbstractCommand> commands = CommandManager.getAllCommands();
            commands.removeIf(command -> command.getCategory() == CommandCategory.HIDDEN);
            if (!message.getAuthor().equals(message.getGuild().getOwner())) {
                commands.removeIf(command -> command.getCategory() == CommandCategory.ADMIN);
            }
            for (CommandCategory c : CommandCategory.values()) {
                StringBuilder sb = new StringBuilder();
                commands.stream().filter(cmd -> cmd.getCategory() == c)
                        .forEach(cmd -> sb.append("`!").append(cmd.getName()).append("`  "));
                if (sb.length() > 0) { //skip over empty categories
                    builder.appendField(c.toString(), sb.toString(), false);       
                }
            }
                    
            BotUtils.sendEmbedMessage(channel, builder.build());            
        }
    }
    
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "", "View available commmands.");
    }

}
