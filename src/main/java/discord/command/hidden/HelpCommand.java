package discord.command.hidden;

import discord.util.BotUtils;
import discord.core.command.CommandManager;
import discord.data.UserManager;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import java.util.HashSet;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

public class HelpCommand extends AbstractCommand {
    
    public HelpCommand() {
        super(new String[] {"help", "commands"}, 0, CommandCategory.HIDDEN);
    }
    
    @Override
    public void execute(IMessage message, String[] args) {
        IChannel channel = message.getChannel();
        if (args.length > 0) {
            String name = args[0].toLowerCase();
            AbstractCommand cmd = CommandManager.getCommand(name);
            if (cmd == null) {
                BotUtils.sendErrorMessage(channel, "Could not find a command by that name. "
                        + "Use `!help` for a list of available commands.");
            } else {
                BotUtils.sendUsageMessage(channel, cmd.getUsage(name));
            }
        } else {
            EmbedBuilder builder = BotUtils.getBuilder(message.getClient(), "Available Commands", 
                    "*For info on a command, use* **`!help [command]`**");
            
            HashSet<AbstractCommand> commands = CommandManager.getAllCommands();          
            commands.removeIf(command -> command.getCategory() == CommandCategory.HIDDEN //Take out hidden
                    || (command.getCategory() == CommandCategory.PERK //take out not unlocked
                    && command.getLevelRequired() > UserManager.getDBUserFromMessage(message).getProgress().getTotalLevelThisLife())
                    || (command.getCategory() == CommandCategory.ADMIN //take out admin if not owner
                    && !message.getAuthor().equals(message.getGuild().getOwner())));
            
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
    
    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "", "View available commmands.");
    }

}
