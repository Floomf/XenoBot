/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package discord.data.object;

import discord.data.UserManager;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.VoiceChannel;


public class XPScheduler {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> future;
    private final XPChecker checker;

    public XPScheduler(XPChecker checker) {
        this.checker = checker;
    }

    public void handleUserVoiceChannelEvent(Guild guild) {
        checkAnyChannelHasEnoughUsers(guild);
    }

    public void checkAnyChannelHasEnoughUsers(Guild guild) {
        boolean hasEnoughUsers = anyChannelHasEnoughUsers(guild);
        if (checkerIsActive() && !hasEnoughUsers) {
            System.out.println("All guild voice channel users <= 1, stopping xp checker");
            future.cancel(true);
            UserManager.saveDatabase();
        } else if (checkerIsNotActive() && hasEnoughUsers) {
            System.out.println("Voice channel users > 1, starting xp checker");
            future = scheduler.scheduleAtFixedRate(checker, 1, 1, TimeUnit.MINUTES);
        }
    }

    private boolean anyChannelHasEnoughUsers(Guild guild) {
        List<VoiceChannel> channels = guild.getChannels().ofType(VoiceChannel.class).collectList().block();
        channels.remove(guild.getAfkChannel().block()); //TODO solve this
        for (VoiceChannel channel : channels) {
            if (channelHasEnoughUsers(channel)) {
                return true;
            }
        }
        return false;
    }

    private boolean channelHasEnoughUsers(VoiceChannel channel) {
        return channel.getVoiceStates()
                .flatMap(VoiceState::getUser)
                .filter(user -> !user.isBot())
                .collectList().block().size() > 1;
    }

    private boolean checkerIsActive() {
        return (future != null && !future.isDone());
    }

    private boolean checkerIsNotActive() {
        return !checkerIsActive();
    }

}
