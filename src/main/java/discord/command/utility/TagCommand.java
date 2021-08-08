package discord.command.utility;

import discord.core.command.InteractionContext;
import discord.util.BotUtils;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.*;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.util.ApplicationCommandOptionType;

public class TagCommand extends AbstractCommand {

    public static final Snowflake GAMES_ROLE_ID = Snowflake.of(621486907620196392L);
    public static int GAMES_ROLE_POSITION;

    public TagCommand() {
        super("tag", 1, CommandCategory.UTILITY);
    }

    @Override
    public ApplicationCommandRequest buildSlashCommand() {
        return ApplicationCommandRequest.builder()
                .name(getName())
                .description("Toggle various tags (roles) on your profile")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("name")
                        .description("The tag to toggle")
                        .type(ApplicationCommandOptionType.STRING.getValue())
                        .required(true)
                        .choices(getTagsAsChoices())
                        .build())
                .build();
    }

    private static List<ApplicationCommandOptionChoiceData> getTagsAsChoices() {
        List<ApplicationCommandOptionChoiceData> choices = new ArrayList<>();
        try {
            Files.lines(Paths.get("tags.txt")).forEachOrdered(line -> {
                if (!line.trim().isEmpty()) {
                    choices.add(ApplicationCommandOptionChoiceData.builder().name(line).value(line).build());
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return choices;
    }

    @Override
    public void execute(InteractionContext context) {
        Snowflake tagRole = context.getGuild().getRoles()
                .filter(role -> role.getName().equals(context.getOptionAsString("name")))
                .map(Role::getId)
                .blockFirst();
        if (tagRole == null) {
           context.replyWithError("A role with that name doesn't exist on this guild. Please create one.");
           return;
        }
        Member member = context.getMember();
        Set<Snowflake> roles = member.getRoleIds();
        if (roles.contains(tagRole)) {
            member.removeRole(tagRole).block();
        } else {
            member.addRole(tagRole).block();
            if (!roles.contains(GAMES_ROLE_ID)) {
                member.addRole(GAMES_ROLE_ID).block();
            }
        }
        context.replyWithInfo("Tag **" + context.getOptionAsString("name") + "** toggled.");
    }

    @Override
    public void execute(Message message, TextChannel channel, String[] args) {
        /*String operation = args[0].toLowerCase();
        String tag;

        Guild guild = message.getGuild().block();

        if (operation.equals("list")) {
            MessageUtils.sendMessage(channel, "Available Tags", "`" + tags.toString() + "`");
            return;
        }

        if (operation.equals("create")) {
            if (!message.getAuthorAsMember().block().equals(guild.getOwner().block())) {
                MessageUtils.sendErrorMessage(channel, "You must be this guild's owner to create tags.");
                return;
            }
            if (args.length < 2) {
                MessageUtils.sendErrorMessage(channel, "Please provide a tag name to create.");
                return;
            }
            tag = CommandHandler.combineArgs(1, args);
            if (tagsContainsIgnoreCase(tag)) {
                MessageUtils.sendErrorMessage(channel, "Tag already exists.");
                return;
            }
            tags.add(tag);
            String finalTag1 = tag;
            message.getGuild().block().createRole(spec -> spec.setName(finalTag1)).block();
            try {
                Files.write(Paths.get("tags.txt"), (tag + "\n").getBytes(), StandardOpenOption.APPEND);
                MessageUtils.sendInfoMessage(channel, "Tag created and saved.");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        tag = CommandHandler.combineArgs(0, args);

        if (!tagsContainsIgnoreCase(tag)) {
            MessageUtils.sendErrorMessage(channel, "That tag doesn't exist. Use `!tag list` to view all tags.");
            return;
        }
        for (String currentTag : tags) { //get the proper case sensitive tag
            if (currentTag.equalsIgnoreCase(tag)) {
                tag = currentTag;
            }
        }
        String finalTag = tag;
        Role tagRole = guild.getRoles().filter(role -> role.getName().equals(finalTag)).collectList().block().get(0); //TODO might break?
        if (tagRole == null) {
            MessageUtils.sendErrorMessage(channel, "A role with that name doesn't exist on guild. Please create one.");
            return;
        }
        Member member = message.getAuthorAsMember().block();
        List<Role> roles = member.getRoles().collectList().block();
        if (roles.contains(tagRole)) {
            member.removeRole(tagRole.getId()).block();
        } else {
            member.addRole(tagRole.getId()).block();
            if (!roles.contains(guild.getRoleById(GAME_ROLE_ID).block())) { //poop
                member.addRole(GAME_ROLE_ID).block();
            }
        }
        MessageUtils.sendInfoMessage(channel, "Tag toggled.");*/
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "(tag name)", "Toggle various tags for your profile."
                + "Tags are essentially just no-permission Discord roles that act as labels."
                + "\n\n**Special Arguments**"
                + "\n`!tag list` - View all available tags.");
    }

    //private boolean tagsContainsIgnoreCase(String tagToCheck) {
    //    return (tags.stream().anyMatch(tag -> tag.equalsIgnoreCase(tagToCheck)));
    //}

}
