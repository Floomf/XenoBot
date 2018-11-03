package discord.command.perk;

import discord.BotUtils;
import discord.ColorManager;
import discord.CommandHandler;
import discord.UserManager;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.object.User;
import discord.object.Unlockable;
import java.util.Arrays;
import java.util.List;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

public class ColorCommand extends AbstractCommand {
    
    public ColorCommand() {
        super(new String[] {"color"}, 1, CommandCategory.PERK);
    }
    
    public void execute(IMessage message, String[] args) {
        IUser dUser = message.getAuthor();
        User user = UserManager.getDBUserFromID(dUser.getLongID());
        IChannel channel = message.getChannel();
        IGuild guild = message.getGuild();
        
        if (!(user.getProgress().getPrestige().getNumber() > 0)) {
            BotUtils.sendErrorMessage(channel, "You must be prestiged to change your name color!"
                    + " You can view your progress with `!prog`.");
            return;
        }
        
        String name = CommandHandler.combineArgs(0, args).toLowerCase();
        
        //handle special arguments
        if (name.equals("list") || name.equals("choices")) {
            EmbedBuilder builder = BotUtils.getBuilder(message.getClient(), "Available Choices", "");
            builder.appendField("Default Colors",
                    "`" + Arrays.toString(ColorManager.getDefaultColors()) + "`", false);
            Unlockable[] unlockedColors = ColorManager.getUnlockedColorsForUser(user);
            builder.appendField("Unlocked Colors [" + unlockedColors.length + "/36]", //TODO hardcoded 36
                    "`" + Arrays.toString(unlockedColors) + "`", false);
            builder.withFooterText((unlockedColors.length == 36) 
                    ? "Holy frick, you've unlocked every color. Congratufuckinglations!" 
                    : "You can keep leveling to unlock more colors.");
            BotUtils.sendEmbedMessage(channel, builder.build());
            return;
        } else if (name.equals("none")) {
            BotUtils.setUserRoles(guild, dUser, getUserRolesNoColors(dUser, guild));
            BotUtils.sendInfoMessage(channel, "Any current saved color has been removed.");
            return;
        }
        
        Unlockable color = ColorManager.getColor(name);
        
        //check if color doesnt exist
        if (color == null) {
            BotUtils.sendErrorMessage(channel, "That color option does not exist.");
            return;
        }
        
        if (!user.hasUnlocked(color)) {
            BotUtils.sendErrorMessage(channel, "You have not unlocked that color yet. "
                    + "You can view your available colors with `!color list`.");
            return;
        }
        
        List<IRole> roles = getUserRolesNoColors(dUser, guild);    
        List<IRole> colorRole = guild.getRolesByName(color.toString());
        
        //make sure color role exists on the guild
        if (colorRole.isEmpty()) {
            BotUtils.sendErrorMessage(channel, "A role by that color name does not exist on this guild.");
            return;
        }
        
        roles.add(colorRole.get(0));
        
        //set their roles, the same as before (minus last color role(s)) plus new color role
        BotUtils.setUserRoles(guild, dUser, roles);
        BotUtils.sendMessage(channel, "Info", "Painted your name in the color " + color + ".",
                colorRole.get(0).getColor()); 
    }
    
    private List<IRole> getUserRolesNoColors(IUser user, IGuild guild) {
        List<IRole> roles = user.getRolesForGuild(guild);      
        //remove any color role(s) they may have
        roles.removeIf(role -> ColorManager.getColor(role.getName()) != null);
        return roles;
    }
    
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "[name]", "Change the color of your name. Colors are unlocked by leveling."
                + "\n*(Prestiged)*"
                + "\n\n**Special Arguments**"
                + "\n`!color list` - View your available colors."
                + "\n`!color none` - Remove your current color.");
    }
    
}
