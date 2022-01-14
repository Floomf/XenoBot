package discord.data.object;

import discord.core.command.InteractionContext;
import discord.data.object.user.DUser;
import discord.data.object.user.Progress;
import discord.util.MessageUtils;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;
import discord4j.core.spec.MessageEditSpec;
import java.util.concurrent.TimeUnit;

public class BadgeMessage {

    protected final Message message;
    protected final Thread idleTimerThread;

    protected boolean active;

    private final DUser dUser;
    private final String[] badges;
    private int badgeIndex;

    public BadgeMessage(InteractionContext context) {
        dUser = context.getDUser();
        badges = dUser.getProg().getBadges();
        badgeIndex = Progress.getBadgeIndex(dUser.getName().getBadge());

        context.event.reply(spec -> {
            spec.addEmbed(MessageUtils.getEmbed("Your Badges", getDisplay())
                    .andThen(embed -> embed.setFooter(getNextBadgeUnlockStats(), "")));
            spec.setComponents(
                    ActionRow.of(Button.secondary("previous", "Previous"), Button.secondary("next", "Next")),
                    ActionRow.of(Button.success("equip", "Equip"), Button.primary("done", "Done")));
        }).block();

        this.message = context.getChannel().getMessageById(
                Snowflake.of(context.event.getInteractionResponse().getInitialResponse().block().id())).block();
        this.idleTimerThread = new Thread(this::startIdleTimer);
        this.active = true;
        idleTimerThread.start();

        context.getChannel().getClient().on(ButtonInteractionEvent.class)
                .takeUntil(p -> !active)
                .filter(event -> event.getMessageId().equals(message.getId()))
                .filter(event -> event.getInteraction().getUser().getId().asLong() == dUser.getDiscordID())
                .subscribe(this::onButton)
        ;
    }

    protected void end() {
        active = false;
        idleTimerThread.interrupt();
    }

    private void onButton(ButtonInteractionEvent event) {
        if (event.getCustomId().equals("equip")) {
            dUser.getName().setBadge(badges[badgeIndex]);
            update(event, "**Badge equipped!**");
        } else if (event.getCustomId().equals("next")) {
            badgeIndex++;
            if (badgeIndex == badges.length) {
                badgeIndex = 0;
            }
            update(event, "");
        } else if (event.getCustomId().equals("previous")) {
            badgeIndex--;
            if (badgeIndex < 0) {
                badgeIndex = badges.length - 1;
            }
            update(event, "");
        } else if (event.getCustomId().equals("done")) {
            end();
            event.edit(InteractionApplicationCommandCallbackSpec.create()
                    .withContent("`Session ended.`")
                    .withEmbeds()
                    .withComponents()).block();
            return;
        }
        idleTimerThread.interrupt();
    }

    private void update(ButtonInteractionEvent event, String message) {
        event.edit(spec ->
                spec.addEmbed(MessageUtils.getEmbed("Your Badges", getDisplay() + "\n\n" + message)
                        .andThen(embed -> embed.setFooter(getNextBadgeUnlockStats(), ""))))
                .block();
    }

    private String getNextBadgeUnlockStats() {
        if (badges.length == Progress.BADGE_UNLOCKS.length) {
            return "(You've unlocked all badges!)";
        }
        return "(Next badge unlock: " + Progress.BADGE_UNLOCKS[badges.length].getStatsRequired() + ")";
    }

    private String getDisplay() {
        StringBuilder badgeDisplay = new StringBuilder();
        for (int i = 0; i < badgeIndex; i++) {
            badgeDisplay.append(" ").append(badges[i]);
        }

        badgeDisplay.append("[").append(badges[badgeIndex]).append("]"); //current badge, shows a selector

        for (int i = badgeIndex + 1; i < badges.length; i++) {
            badgeDisplay.append(badges[i]).append(" ");
        }

        badgeDisplay.append("\n\nSelected: ").append(badges[badgeIndex]);
        return badgeDisplay.toString();
    }

    protected void startIdleTimer() {
        try {
            Thread.sleep(TimeUnit.MINUTES.toMillis(1));
            message.edit(MessageEditSpec.create()
                    .withContentOrNull("`Session ended.`")
                    .withEmbeds()
                    .withComponents()).block();
        } catch (InterruptedException e) {
            if (active) {
                startIdleTimer();
            } else {
                Thread.currentThread().interrupt();
            }
        }
    }

}
