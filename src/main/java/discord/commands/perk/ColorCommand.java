package discord.commands.perk;

import discord.BotUtils;
import discord.CommandHandler;
import discord.UserManager;
import discord.commands.AbstractCommand;
import discord.commands.CommandCategory;
import discord.objects.User;
import java.util.Arrays;
import java.util.List;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;

public class ColorCommand extends AbstractCommand {
    
    //hardcoded IDs, guild contains roles for each one
    private final String[] COLORS = {"Dark Red", "Red", "Dark Orange", "Orange", "Gold", "Yellow",
        "Green Yellow", "Mint", "Lime", "Green", "Dark Green", "Turqoise", "Teal", "Light Blue", 
        "Blue", "Dark Blue", "Indigo", "Violet", "Purple", "Magenta", "Hot Pink", "Pink",
        "Light Brown", "Brown", "Gray"};
    
    public ColorCommand() {
        super(new String[] {"color"}, 1, CommandCategory.PERK);
    }
    
    public void execute(IMessage message, String[] args) {
        IUser dUser = message.getAuthor();
        User user = UserManager.getUserFromID(dUser.getLongID());
        IChannel channel = message.getChannel();
        if (!(user.getPrestige() > 0)) {
            BotUtils.sendErrorMessage(channel, "You must be prestiged to set your name color!"
                    + " You can view your level progress with `!lvl`.");
            return;
        }
        //get the color from message (combine args)
        String color =  CommandHandler.combineArgs(0, args).toLowerCase();       
        for (int i = 0; i < COLORS.length; i++) {
            if (COLORS[i].toLowerCase().equals(color)) {
                IGuild guild = message.getGuild();
                List<IRole> roles = dUser.getRolesForGuild(guild);
                //remove color role(s) they may already have
                roles.removeIf(role -> Arrays.asList(COLORS).contains(role.getName()));
                roles.add(guild.getRolesByName(COLORS[i]).get(0));
                //set their roles, the same as before (minus last color role) plus new color role
                BotUtils.setRoles(guild, dUser, roles.toArray(new IRole[roles.size()]));
                BotUtils.sendInfoMessage(channel,
                        String.format("Your name is now the color %s!", color));
                return;
            }
        }
        BotUtils.sendMessage(channel, "Unknown color! Available choices:", Arrays.toString(COLORS));
        return;
    }
    
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "[name]", "Change the color of your name."
                + "\n*(Prestiged)*\n\n**Available Choices**:\n`" + Arrays.toString(COLORS) + "`");
    }
    
}
