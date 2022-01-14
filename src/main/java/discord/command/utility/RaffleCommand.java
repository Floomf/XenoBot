package discord.command.utility;

import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.core.command.InteractionContext;
import discord.util.BotUtils;

import java.util.List;

import discord.util.MessageUtils;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.core.object.presence.Status;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.core.object.command.ApplicationCommandOption;

public class RaffleCommand extends AbstractCommand {

    public RaffleCommand() {
        super("raffle", 1, CommandCategory.UTILITY);
    }

    @Override
    public ApplicationCommandRequest buildSlashCommand() {
        return ApplicationCommandRequest.builder()
                .name(getName())
                .description("Randomly pick a user from a specified pool (Not including bots)")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("pool")
                        .description("The pool to pick from")
                        .required(true)
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .addChoice(ApplicationCommandOptionChoiceData.builder().name("all").value("all").build())
                        .addChoice(ApplicationCommandOptionChoiceData.builder().name("online").value("online").build())
                        .addChoice(ApplicationCommandOptionChoiceData.builder().name("voice").value("voice").build())
                        .build())
                .build();
    }

    @Override
    public void execute(InteractionContext context) {
        String poolType = context.getOptionAsString("pool");

        List<Member> raffleUsers = context.getGuild().getMembers().collectList().block();

        if (poolType.equals("online")) {
            raffleUsers.removeIf(user -> user.getPresence().block().getStatus().equals(Status.OFFLINE));
        } else if (poolType.equals("voice")) {
            if (context.getMember().getVoiceState().blockOptional().isEmpty()) {
                context.replyWithError("You must connect to a voice channel on this server first!");
                return;
            }
            raffleUsers = context.getMember().getVoiceState().block().getChannel().block()
                    .getVoiceStates().flatMap(VoiceState::getMember).collectList().block();
        }

        raffleUsers.removeIf(User::isBot);

        if (raffleUsers.isEmpty()) {
            context.replyWithError("There aren't any users in the selected pool!");
            return;
        }

        Member winner = raffleUsers.get((int) (Math.random() * raffleUsers.size()));
        context.reply(MessageUtils.getEmbed("Winner! 🎉", winner.getMention(), winner.getColor().block())
                .andThen(embed -> embed.setImage(winner.getAvatarUrl())));
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

    @Override
    public boolean isSupportedGlobally() {
        return true;
    }

}
