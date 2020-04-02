package discord.command.perk;

import discord.data.object.user.Progress;
import discord.util.BotUtils;
import discord.data.ColorManager;
import discord.core.command.CommandHandler;
import discord.data.UserManager;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.data.object.user.DUser;
import discord.data.object.Unlockable;

import java.awt.*;
import java.util.Arrays;
import java.util.Set;

import discord.util.MessageUtils;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.util.Snowflake;
import discord4j.core.object.entity.Message;

public class ColorCommand extends AbstractCommand {

    public static final int LEVEL_REQUIRED = Progress.MAX_LEVEL;

    public ColorCommand() {
        super(new String[]{"color"}, 1, CommandCategory.PERK);
    }

    @Override
    public void execute(Message message, TextChannel channel, String[] args) {
        DUser user = UserManager.getDUserFromMessage(message);
        Member member = message.getAuthorAsMember().block();

        String name = CommandHandler.combineArgs(0, args).toLowerCase();

        //handle special arguments
        if (name.equals("list") || name.equals("choices")) {
            Unlockable[] unlockedColors = ColorManager.getUnlockedColorsForDUser(user);
            channel.createMessage(spec -> spec.setEmbed(MessageUtils.message("Available Colors", "", Color.WHITE)
                    .andThen(embed -> {
                        embed.addField("Default", "`" + Arrays.toString(ColorManager.getDefaultColors()) + "`", false);
                        embed.addField("Unlocked [" + unlockedColors.length + "/" + ColorManager.COLORS_UNLOCKS.length + "]",
                                "`" + Arrays.toString(unlockedColors) + "`", false);
                        embed.setFooter(unlockedColors.length == ColorManager.COLORS_UNLOCKS.length
                                ? "You've unlocked every color. Astounding."
                                : "You can keep leveling to unlock more colors.", "");
                    }))).block();
            return;
        } else if (name.equals("none")) {
            message.getAuthorAsMember().block().edit(spec -> spec.setRoles(ColorManager.getMemberRolesNoColor(message.getAuthorAsMember().block()))).block();
            MessageUtils.sendInfoMessage(channel, "Any current saved color has been removed.");
            return;
        }

        //check if color doesnt exist
        if (!ColorManager.isColor(name)) {
            MessageUtils.sendErrorMessage(channel, "That color option does not exist.");
            return;
        }

        Unlockable color = ColorManager.getColor(name);

        if (!user.hasUnlocked(color)) {
            MessageUtils.sendErrorMessage(channel, "You haven't unlocked that color yet. "
                    + "You can view your available colors with `!color list`.");
            return;
        }

        Set<Snowflake> roles = ColorManager.getMemberRolesNoColor(member);

        /*
        //make sure color role exists on the guild
        if (colorRole.isEmpty()) {
            BotUtils.sendErrorMessage(channel, "A role by that color name does not exist on this guild. "
                    + "Please create one with the appropriate color.");
            return;
        }*/

        Role colorRole = BotUtils.getGuildRole(name, message.getGuild().block());
        roles.add(colorRole.getId());

        //set their roles, the same as before (minus last color role(s)) plus new color role
        member.edit(spec -> spec.setRoles(roles)).block();
        MessageUtils.sendMessage(channel, "Info", "Painted your name in the color " + color + ".",
                colorRole.getColor());
    }

    @Override
    public int getLevelRequired() {
        return LEVEL_REQUIRED;
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "[name]", "Change the color of your name on this guild. "
                + "New colors are unlocked by leveling."
                + "\n\n**Special Arguments**"
                + "\n`!color list` - View your available colors."
                + "\n`!color none` - Remove your current color.");
    }

}
