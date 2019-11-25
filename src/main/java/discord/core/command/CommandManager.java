package discord.core.command;

import discord.command.AbstractCommand;

import java.util.HashMap;
import java.util.HashSet;

import org.reflections.*;

public class CommandManager {

    public static final String CMD_PREFIX = "!";

    private static final HashMap<String, AbstractCommand> commands = new HashMap<>();

    public static void createCommands() {
        Reflections reflections = new Reflections("discord.command");
        for (Class<? extends AbstractCommand> command : reflections.getSubTypesOf(AbstractCommand.class)) {
            try {
                AbstractCommand cmd = command.newInstance();
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

    public static HashSet<AbstractCommand> getAllCommands() {
        return new HashSet<>(commands.values());
    }

}
