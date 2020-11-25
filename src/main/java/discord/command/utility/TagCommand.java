package discord.command.utility;

import discord.util.BotUtils;
import discord.core.command.CommandHandler;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import discord.util.MessageUtils;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.*;
import discord4j.core.object.entity.channel.TextChannel;

public class TagCommand extends AbstractCommand {

    private static final Snowflake GAME_ROLE_ID = Snowflake.of(621486907620196392L);

    private ArrayList<String> tags = new ArrayList<>();

    public TagCommand() {
        super(new String[]{"tag", "label"}, 1, CommandCategory.UTILITY);
        try {
            Files.lines(Paths.get("tags.txt")).forEachOrdered(line -> {
                if (!line.trim().isEmpty()) {
                    tags.add(line);
                }
            });
            System.out.println(tags.size() + " tags loaded.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void execute(Message message, TextChannel channel, String[] args) {
        String operation = args[0].toLowerCase();
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
        MessageUtils.sendInfoMessage(channel, "Tag toggled.");
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "(tag name)", "Toggle various tags for your profile."
                + "Tags are essentially just no-permission Discord roles that act as labels."
                + "\n\n**Special Arguments**"
                + "\n`!tag list` - View all available tags.");
    }

    private boolean tagsContainsIgnoreCase(String tagToCheck) {
        return (tags.stream().anyMatch(tag -> tag.equalsIgnoreCase(tagToCheck)));
    }

}
