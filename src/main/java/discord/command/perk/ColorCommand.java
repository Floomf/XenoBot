package discord.command.perk;

import discord.manager.ShopManager;
import discord.data.object.ShopItem;
import discord.data.object.user.Progress;
import discord.util.BotUtils;
import discord.manager.ColorManager;
import discord.core.command.CommandHandler;
import discord.manager.UserManager;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.data.object.user.DUser;
import discord.data.object.Unlockable;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import discord.util.MessageUtils;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.entity.Message;
import discord4j.core.util.OrderUtil;
import discord4j.rest.util.Color;

public class ColorCommand extends AbstractCommand {

    public static final int LEVEL_REQUIRED = Progress.MAX_LEVEL;

    public ColorCommand() {
        super(new String[]{"color"}, 1, CommandCategory.PERK);
    }

    @Override
    public void execute(Message message, TextChannel channel, String[] args) {
        DUser user = UserManager.getDUserFromMessage(message);
        Member member = message.getAuthorAsMember().block();

        String name = CommandHandler.combineArgs(0, args).toLowerCase().replace("@", "");

        //handle special arguments
        if (name.equals("list") || name.equals("choices")) {
            Unlockable[] unlockedColors = ColorManager.getUnlockedColorsForDUser(user);
            List<String> purchasedColors = user.getPurchases().stream().filter(item -> item.getCategory() == ShopItem.Category.NAME_COLOR)
                    .map(ShopItem::getName).collect(Collectors.toList());

            List<Role> roles = OrderUtil.orderRoles(message.getGuild().block().getRoles()).collectList().block();

            List<String> defaultColorMentions = roles.stream().filter(role -> ColorManager.isDefaultColor(role.getName()))
                    .map(Role::getMention).collect(Collectors.toList());
            List<String> unlockedColorMentions = roles.stream().filter(role -> ColorManager.colorIsInArray(unlockedColors, role.getName()))
                    .map(Role::getMention).collect(Collectors.toList());
            List<String> purchasedColorMentions = roles.stream().filter(role -> purchasedColors.contains(role.getName()))
                    .map(Role::getMention).collect(Collectors.toList());

            //We have to reverse them due to the way they are ordered on discord
            Collections.reverse(defaultColorMentions);
            Collections.reverse(unlockedColorMentions);
            Collections.reverse(purchasedColorMentions);

            channel.createEmbed(MessageUtils.getEmbed("Available Colors", "", Color.WHITE)
                    .andThen(embed -> {
                        embed.addField("Default", defaultColorMentions.toString().replace("[", "").replace("]", ""), false);
                        if (unlockedColorMentions.size() > 0) {
                            embed.addField("Unlocked [" + unlockedColors.length + "/" + ColorManager.COLORS_UNLOCKS.length + "]",
                                    unlockedColorMentions.toString().replace("[", "").replace("]", ""), false);
                        }
                            embed.addField("Purchased [" + purchasedColorMentions.size() + "/9]", //hardcoded
                                    purchasedColorMentions.isEmpty() ? "`!shop`"
                                            : purchasedColorMentions.toString().replace("[", "").replace("]", ""), false);
                        embed.setFooter(unlockedColors.length == ColorManager.COLORS_UNLOCKS.length
                                ? "You've unlocked every unlockable color. Astounding."
                                : (user.getProg().isMaxLevel() ? "Prestige to unlock more colors."
                                : "Keep leveling to unlock more colors."), "");
                    })).block();
            return;
        } else if (name.equals("none")) {
            message.getAuthorAsMember().block().edit(spec -> spec.setRoles(ColorManager.getMemberRolesNoColor(message.getAuthorAsMember().block()))).block();
            MessageUtils.sendInfoMessage(channel, "Any current saved color has been removed.");
            return;
        }

        //check if color doesnt exist
        if (!(ColorManager.isUnlockedColor(name) || ShopManager.isPurchasedColor(name))) {
            MessageUtils.sendErrorMessage(channel, "That color option does not exist.");
            return;
        }

        //Unlockable color = ColorManager.getColor(name);

        if (ColorManager.isUnlockedColor(name) && !user.hasUnlocked(ColorManager.getColor(name))) {
            MessageUtils.sendErrorMessage(channel, "You haven't unlocked that color yet. "
                    + "You can view your available colors with `!color list`.");
            return;
        } else if (ShopManager.isPurchasedColor(name) && !user.hasPurchased(name)) {
            MessageUtils.sendErrorMessage(channel, "You haven't purchased that color yet. "
                    + "You can view what colors you can buy using  `!shop`.");
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
        MessageUtils.sendMessage(channel, "Info", "Painted your name in the color " + colorRole.getMention() + ".",
                colorRole.getColor());
    }

    @Override
    public int getLevelRequired() {
        return LEVEL_REQUIRED;
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "[name]", "Change the color of your name on this guild. "
                + "New colors are unlocked by leveling up or purchasing them."
                + "\n\n**Special Arguments**"
                + "\n`!color list` - View your available colors."
                + "\n`!color none` - Remove your current color.");
    }

}
