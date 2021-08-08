package discord.core.command;

import discord.command.AbstractCommand;

import java.util.HashMap;
import java.util.HashSet;

import discord.listener.EventsHandler;
import discord4j.common.util.Snowflake;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.RestClient;
import org.reflections.*;
import reactor.core.publisher.Mono;

public class CommandManager {

    public static final String CMD_PREFIX = "!";

    private static final HashMap<String, AbstractCommand> commands = new HashMap<>();

    public static void createCommands() {
        Reflections reflections = new Reflections("discord.command");
        for (Class<? extends AbstractCommand> command : reflections.getSubTypesOf(AbstractCommand.class)) {
            try {
                AbstractCommand cmd = command.newInstance();
                commands.put(cmd.getName(), cmd);
            } catch (InstantiationException | IllegalAccessException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void registerGuildSlashCommands(RestClient restClient, Snowflake guildID) {
        if (guildID.equals(EventsHandler.THE_REALM_ID)) {
            registerSlashCommands(restClient);
        } else {
            commands.values().stream().forEach(command -> {
                ApplicationCommandRequest request = command.buildOutsideGuildSlashCommand();
                if (request != null) {
                    restClient.getApplicationService()
                            .createGuildApplicationCommand(restClient.getApplicationId().block(), guildID.asLong(), request).onErrorResume(e -> Mono.empty()).block();
                }
            });
        }
    }

    public static void registerSlashCommands(RestClient restClient) {
       commands.values().forEach(command -> {
           ApplicationCommandRequest request = command.buildSlashCommand();
           if (request != null) {
               if (command.isSupportedGlobally()) {
                   restClient.getApplicationService()
                           .createGlobalApplicationCommand(restClient.getApplicationId().block(), request).block();
               } else {
                   restClient.getApplicationService()
                           .createGuildApplicationCommand(restClient.getApplicationId().block(),
                                   EventsHandler.THE_REALM_ID.asLong(), request).block();
               }
           }
       });
    }

    public static AbstractCommand getCommand(String name) {
        return commands.get(name);
    }

    public static HashSet<AbstractCommand> getAllCommands() {
        return new HashSet<>(commands.values());
    }

    public static long getGlobalCommandsCount() {
        return commands.values().stream().filter(command -> command.isSupportedGlobally() || command.buildOutsideGuildSlashCommand() != null).count();
    }

}
