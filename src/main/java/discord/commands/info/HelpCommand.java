package discord.commands.info;

import discord.BotUtils;
import discord.CommandManager;
import discord.commands.AbstractCommand;
import java.util.HashSet;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;

public class HelpCommand extends AbstractCommand {
    
    public HelpCommand() {
        super(new String[] {"help", "commands"}, 0, false);
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
            HashSet<AbstractCommand> commands = CommandManager.getCommands();
            commands.removeIf(command -> command.equals(this)); //temp solution?
            if (!message.getAuthor().equals(message.getGuild().getOwner())) {
                commands.removeIf(command -> command.onlyOwner());
            }
            StringBuilder sb = new StringBuilder();
            for (AbstractCommand command : commands) {
                sb.append("`").append(command.getName()).append("`   ");
            }
            sb.append("\n\n*To view usage and info on a command, use:* \n**!help [command]**");
            BotUtils.sendMessage(channel, "Available Commands", sb.toString());
            
        }
    }
    
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "", "View available commmands.");
    }

}
