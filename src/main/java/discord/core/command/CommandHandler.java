package discord.core.command;

import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.manager.GameManager;
import discord.manager.UserManager;
import discord.data.object.user.Progress;
import discord.listener.EventsHandler;
import discord.util.MessageUtils;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.*;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandHandler {

    public static void onMessageEvent(MessageCreateEvent event) {
        processCommand(event.getMessage());
    }

    public static void processCommand(Message message) {
        TextChannel channel = message.getChannel().ofType(TextChannel.class).block();
        if (channel == null || !message.getAuthor().isPresent()
                || message.getAuthor().get().isBot()
                || !message.getContent().startsWith(CommandManager.CMD_PREFIX)) {
            return;
        }

        if (!(channel.getName().contains("command") || channel.getName().contains("bot"))) {
            message.addReaction(ReactionEmoji.unicode("❌")).block();
            return;
        }

        //block all commands when in type game
        if (GameManager.playerIsInTypingGame(message.getAuthorAsMember().block())) {
            return;
        }

        //separate the contents of the message into a list of strings
        //separate by space characters, and group quoted sections into their own element
        ArrayList<String> contents = new ArrayList<>();
        Matcher m = Pattern.compile("([“\"][^\"”“]+[”\"]|\\S+)").matcher(message.getContent().trim());
        while (m.find()) {
            contents.add(m.group(1).replaceAll("[“\"”]", "").trim()); //remove any quote characters
        }

        if (contents.isEmpty()) {
            return;
        }

        //get command from name
        String name = contents.get(0).substring(1).toLowerCase();
        AbstractCommand command = CommandManager.getCommand(name);

        //make sure command exists
        if (command == null) {
            //MessageUtils.sendErrorMessage(channel,"Unknown command. Type `!help` for available commands.");
            message.addReaction(ReactionEmoji.unicode("❔")).block();
            return;
        }

        LoggerFactory.getLogger(CommandHandler.class).info(message.getAuthor().get().getTag() + " issued " + message.getContent()
                + " on " + channel.getGuild().block().getName());

        if (!command.isSupportedGlobally() && !channel.getGuildId().equals(EventsHandler.THE_REALM_ID)) {
            MessageUtils.sendErrorMessage(channel, "That command isn't supported on this guild. Sorry!");
            return;
        }

        //check if command requires owner (and if owner is executing it)
        if (command.getCategory().equals(CommandCategory.ADMIN) &&
                !message.getAuthorAsMember().block().equals(channel.getGuild().block().getOwner().block())) {
            MessageUtils.sendErrorMessage(channel, "You must be this guild's owner to use this command.");
            return;
        }

        //check if command requires certain level
        Progress progress = UserManager.getDUserFromMessage(message).getProg();
        if (command.getLevelRequired() > progress.getTotalLevelThisLife()) {
            MessageUtils.sendErrorMessage(channel, "You must be level **" + command.getLevelRequired()
                    + "** to use this command! You can use `!prog` to view your progress.");
            return;
        }

        //leave only the args in the arraylist
        contents.remove(0);

        //check if command needs args (and if those args exist)
        if (contents.size() < command.getArgsRequired()) {
            MessageUtils.sendUsageMessage(channel, command.getUsage(name));
            return;
        }

        //create new array with only args (no args = empty array);
        String[] args = contents.toArray(new String[contents.size()]);
        command.execute(message, channel, args);
    }

    public static String combineArgs(int index, String[] args) {
        for (int i = index + 1; i < args.length; i++) {
            args[index] += " " + args[i];
        }
        return args[index];
    }

}