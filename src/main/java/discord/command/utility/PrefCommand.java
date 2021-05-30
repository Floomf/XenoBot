package discord.command.utility;

import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.core.command.InteractionContext;
import discord.manager.UserManager;
import discord.data.object.user.Pref;
import discord.util.BotUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import discord.util.MessageUtils;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.util.ApplicationCommandOptionType;

public class PrefCommand extends AbstractCommand {

    public PrefCommand() {
        super(new String[]{"preference"}, 1, CommandCategory.UTILITY);
    }

    @Override
    public ApplicationCommandRequest buildSlashCommand() {
        return ApplicationCommandRequest.builder()
                .name("preference")
                .description("Change one of your preferences")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("pref")
                        .description("The preference")
                        .required(true)
                        .type(ApplicationCommandOptionType.STRING.getValue())
                        .addAllChoices(getPrefsAsChoices())
                        .build())
                .addOption(ApplicationCommandOptionData.builder()
                        .name("value")
                        .description("true/false")
                        .required(true)
                        .type(ApplicationCommandOptionType.BOOLEAN.getValue())
                        .build())
                .build();
    }

    private List<ApplicationCommandOptionChoiceData> getPrefsAsChoices() {
        List<ApplicationCommandOptionChoiceData> choices = new ArrayList<>();
        for (Pref pref : Pref.values()) {
            choices.add(ApplicationCommandOptionChoiceData.builder()
                    .name(pref.name().toLowerCase())
                    .value(pref.name().toLowerCase())
                    .build());
        }
        return choices;
    }

    @Override
    public void execute(InteractionContext context) {
        Pref pref = Pref.valueOf(context.getOptionAsString("pref").toUpperCase());
        boolean update = context.getOptionAsBoolean("value").orElse(false);

        context.getDUser().getPrefs().put(pref, update);
        UserManager.saveDatabase();

        context.replyWithInfo("`" + pref.toString() + "` has been set to `" + update + "`.");
    }

    @Override
    public void execute(Message message, TextChannel channel, String[] args) {
        String name = args[0];
        HashMap<Pref, Boolean> userPrefs = UserManager.getDUserFromMessage(message).getPrefs();

        if (name.equalsIgnoreCase("list")) {
            String prefList = "";
            for (Pref pref : userPrefs.keySet()) {
                prefList += String.format("**%s** - `%s`\n", pref.toString(), userPrefs.get(pref));
            }
            MessageUtils.sendMessage(channel, "Your Preferences", prefList);
            return;
        }

        if (args.length < 2) {
            MessageUtils.sendErrorMessage(channel, "Please specify a preference name with a boolean value following it.");
            return;
        }

        if (!Pref.contains(args[0])) {
            MessageUtils.sendErrorMessage(channel, "Unknown preference name. "
                    + "To view a list of available preferences, use `!pref list`."); //Doesnt enforce different prefixes
            return;
        }

        if (!(args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("false"))) {
            MessageUtils.sendErrorMessage(channel,
                    "Please specify enabling/disabling a preference with `true` or `false`.");
            return;
        }

        Pref pref = Pref.valueOf(args[0].toUpperCase());
        Boolean bool = Boolean.valueOf(args[1].toLowerCase());

        userPrefs.put(pref, bool);

        MessageUtils.sendInfoMessage(channel, "`" + pref.toString() + "` has been set to `" + bool.toString() + "`.");
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "[name] [true/false]", "Change a preference for yourself on this guild."
                + "\n\n**Special Arguments**"
                + "\n`!" + alias + " list` - View a list of your preferences.");
    }

}
