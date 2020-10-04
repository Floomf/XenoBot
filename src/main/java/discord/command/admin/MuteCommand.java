package discord.command.admin;

import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.util.BotUtils;
import discord.util.DiscordColor;
import discord.util.MessageUtils;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.TextChannel;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class MuteCommand extends AbstractCommand {

    public MuteCommand() {
        super(new String[]{"mute", "silence"}, 2, CommandCategory.ADMIN);
    }

    @Override
    public void execute(Message message, TextChannel channel, String[] args) {
        List<User> users = message.getUserMentions().filter(user -> !user.isBot()).collectList().block();

        if (users.isEmpty()) {
            MessageUtils.sendErrorMessage(channel, "Couldn't identify any user. Please @mention them.");
            return;
        }

        //check xp amount
        int seconds;
        try {
            seconds = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            MessageUtils.sendErrorMessage(channel, "Couldn't parse a valid amount of seconds.");
            return;
        }

        Member mutedMember = users.get(0).asMember(message.getGuild().block().getId()).block();
        mutedMember.edit(spec -> spec.setMute(true)).block();

        channel.createMessage(spec -> {
            spec.setContent(mutedMember.getMention());
            spec.setEmbed(MessageUtils.getEmbed("Oh no!", "You've been voice muted for **" + seconds + "** seconds.",
                    DiscordColor.RED));
        }).block();

        new Timer().schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        mutedMember.edit(spec -> spec.setMute(false)).block();
                        channel.createMessage(spec -> {
                            spec.setContent(mutedMember.getMention());
                            spec.setEmbed(MessageUtils.getEmbed("Nice.", "You've been unmuted!", DiscordColor.GREEN));
                        }).block();
                    }
                }, TimeUnit.SECONDS.toMillis(seconds)
        );
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "@mention (seconds)", "Voice mute someone temporarily.");
    }
}
