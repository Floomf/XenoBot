package discord.command.utility;

import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.util.BotUtils;

import java.util.List;

import discord.util.MessageUtils;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.presence.Status;

public class RaffleCommand extends AbstractCommand {

    public RaffleCommand() {
        super(new String[]{"raffle", "giveaway"}, 1, CommandCategory.UTILITY);
    }

    @Override
    public void execute(Message message, TextChannel channel, String[] args) {
        String poolType = args[0].toLowerCase();

        if (!poolType.matches("all|online|voice")) {
            MessageUtils.sendErrorMessage(channel, "Unknown pool type. Type `!raffle` for help.");
            return;
        }

        List<User> raffleUsers = message.getGuild().block().getMembers().cast(User.class).collectList().block();

        if (poolType.equals("online")) {
            raffleUsers.removeIf(user -> ((Member) user).getPresence().block().getStatus().equals(Status.OFFLINE));
        } else if (poolType.equals("voice")) {
            if (message.getAuthorAsMember().block().getVoiceState().block() == null) {
                MessageUtils.sendErrorMessage(channel, "You aren't connected to any voice channel on this guild.");
                return;
            }
            raffleUsers = message.getAuthorAsMember().block().getVoiceState().block().getChannel().block()
                    .getVoiceStates().flatMap(VoiceState::getUser).collectList().block();
        }

        raffleUsers.removeIf(user -> user.equals(message.getAuthorAsMember().block()) || user.isBot());

        if (raffleUsers.isEmpty()) {
            MessageUtils.sendErrorMessage(channel, "There aren't any valid users in the selected pool!");
            return;
        }

        Member winner = raffleUsers.get((int) (Math.random() * raffleUsers.size())).asMember(message.getGuild().block().getId()).block();
        channel.createEmbed(MessageUtils.getEmbed("Winner! 🎉", winner.getMention(), winner.getColor().block())
                .andThen(embed -> embed.setImage(winner.getAvatarUrl()))).block();
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
