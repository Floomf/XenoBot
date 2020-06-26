package discord.core.command;

import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.core.game.GameManager;
import discord.core.game.TypeGame;
import discord.data.UserManager;
import discord.data.object.user.Progress;
import discord.util.MessageUtils;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.*;
import discord4j.core.object.reaction.ReactionEmoji;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandHandler {

    public static void onMessageEvent(MessageCreateEvent event) {
        Message message = event.getMessage();
        TextChannel channel = message.getChannel().ofType(TextChannel.class).block();

        if (channel == null || !(message.getContent().orElse("").startsWith(CommandManager.CMD_PREFIX))) {
            return;
        }

        if (!(channel.getName().equals("commands") || channel.getName().equals("testing"))) {
            event.getMessage().addReaction(ReactionEmoji.unicode("❌")).block();
            return;
        }

        //block starting new games when in type game
        if (GameManager.getGames().stream().anyMatch(game -> game.isActive() && //TEMPORARY
                game instanceof TypeGame && game.playerIsInGame(message.getAuthorAsMember().block()))) {
            return;
        }

        //separate the contents of the message into a list of strings
        //separate by space characters, and group quoted sections into their own element
        ArrayList<String> contents = new ArrayList<>();
        Matcher m = Pattern.compile("([“\"][^\"”“]+[”\"]|\\S+)").matcher(message.getContent().get().trim());
        while (m.find()) {
            contents.add(m.group(1).replaceAll("[“\"”]", "").trim()); //remove any quote characters
        }

        if (contents.isEmpty()) {
            return;
        }

        /*
        //make sure the guild has a commands channel
        List<Channel> commandChannels = guild.getChannelsByName("commands");
        if (commandChannels.isEmpty()) {
            BotUtils.sendInfoMessage(channel,
                    "Please create a new text channel named `#commands`!"
                            + " I will only function properly there. Beep boop.");
            return;
        }

        //make sure command is in the commands channel
        if (!channel)) {
            BotUtils.sendInfoMessage(channel,
                    "I will only respond to commands within the <#" + commandChannels.get(0).getLongID() + "> chat."
                            + " Please type your command again there.");
            return;
        }*/

        //get command from name
        String name = contents.get(0).substring(1).toLowerCase();
        AbstractCommand command = CommandManager.getCommand(name);

        //make sure command exists
        if (command == null) {
            //MessageUtils.sendErrorMessage(channel,"Unknown command. Type `!help` for available commands.");
            event.getMessage().addReaction(ReactionEmoji.unicode("❔")).block();
            return;
        }

        System.out.println(message.getAuthorAsMember().block().getDisplayName() + " issued " + message.getContent().get());

        //check if command requires owner (and if owner is executing it)
        if (command.getCategory().equals(CommandCategory.ADMIN) &&
                !message.getAuthorAsMember().block().equals(message.getGuild().block().getOwner().block())) {
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