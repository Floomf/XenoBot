package discord;

import discord.commands.AbstractCommand;
import java.util.HashMap;
import java.util.HashSet;

import org.reflections.*;

public class CommandManager {

    private static HashMap<String, AbstractCommand> commands = new HashMap<>();
    
    public static void createCommands() {       
        Reflections reflections = new Reflections("discord.commands");
        for (Class<?> command : reflections.getSubTypesOf(AbstractCommand.class)) {
            try {
                AbstractCommand cmd = (AbstractCommand) command.newInstance();
                for (String alias : cmd.getNames()) {
                    //keys are command names, all point to same command object
                    commands.put(alias, cmd);
                }
            } catch (InstantiationException | IllegalAccessException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    public static AbstractCommand getCommand(String name) {
        return commands.get(name);
    }   
    
    public static HashSet<AbstractCommand> getCommands() {
        return new HashSet<AbstractCommand>(commands.values());
    }
    
}
