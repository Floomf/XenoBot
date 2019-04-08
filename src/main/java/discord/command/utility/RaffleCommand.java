package discord.command.utility;

import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.util.BotUtils;
import java.util.List;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.StatusType;

public class RaffleCommand extends AbstractCommand {

    public RaffleCommand() {
        super(new String[]{"raffle", "giveaway"}, 1, CommandCategory.UTILITY);
    }

    @Override
    public void execute(IMessage message, String[] args) {
        String poolType = args[0].toLowerCase();
        
        if (!poolType.matches("all|online|voice")) {
            BotUtils.sendErrorMessage(message.getChannel(),
                    "Unknown pool type. Type `!raffle` for help.");
            return;
        }
        
        List<IUser> raffleUsers = message.getGuild().getUsers();
        
        if (poolType.equals("online")) {
            raffleUsers.removeIf(user -> user.getPresence().getStatus().equals(StatusType.OFFLINE));
        } else if (poolType.equals("voice")) {
            if (message.getAuthor().getVoiceStateForGuild(message.getGuild()).getChannel() == null) {
                BotUtils.sendErrorMessage(message.getChannel(), "You aren't connected to any voice channel on this guild.");
                return;
            }
            raffleUsers = message.getAuthor().getVoiceStateForGuild(message.getGuild()).getChannel().getConnectedUsers();
        }     

        raffleUsers.removeIf(user -> (user.equals(message.getAuthor()) || user.isBot()));

        if (raffleUsers.isEmpty()) {
            BotUtils.sendErrorMessage(message.getChannel(), "There aren't any valid users in the selected pool!");
            return;
        }

        IUser winner = raffleUsers.get((int) (Math.random() * raffleUsers.size()));
        BotUtils.sendEmbedMessage(message.getChannel(), BotUtils.getBuilder(message.getClient(), "Winner! 🎉", winner.mention())
                .withImage(winner.getAvatarURL()).build());
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "[all/online/voice]",
                "Randomly pick a user in the specified pool (not including yourself or bots)."
                + "\n\n**Pool Types:**"
                + "\n`all` - All server users."
                + "\n`online` - All server users that are online."
                + "\n`voice` - All users in your currently connected voice channel.");
    }

}
