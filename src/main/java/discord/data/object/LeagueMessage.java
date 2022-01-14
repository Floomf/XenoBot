package discord.data.object;

import discord.command.fun.LeagueCommand;
import discord.core.command.InteractionContext;
import discord.data.object.user.DUser;
import discord.data.object.user.Progress;
import discord.util.DiscordColor;
import discord.util.MessageUtils;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.*;
import kong.unirest.json.JSONObject;

import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class LeagueMessage {

    protected final Message message;
    protected final Thread idleTimerThread;

    protected final JSONObject matchData;
    protected final ArrayList<JSONObject> teamData;

    protected boolean active;

    private final DUser dUser;
    private int playerIndex;

    public LeagueMessage(InteractionContext context, JSONObject matchData, JSONObject pData, ArrayList<JSONObject> teamData) {
        dUser = context.getDUser();

        for (int i = 0; i < teamData.size(); i++) {
            if (teamData.get(i).getString("summonerName").equals(pData.getString("summonerName"))) {
                playerIndex = i;
                break;
            }
        }

        this.matchData = matchData;
        this.teamData = teamData;

        context.event.editReply(InteractionReplyEditSpec.create()
                .withEmbeds(getPlayerEmbed(matchData, pData))
                .withComponents(ActionRow.of(Button.secondary("previous", "Previous Teammate"),
                        Button.secondary("next", "Next Teammate")))).block();

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
        if (event.getCustomId().equals("next")) {
            playerIndex++;
            if (playerIndex == teamData.size()) {
                playerIndex = 0;
            }
            update(event);
        } else if (event.getCustomId().equals("previous")) {
            playerIndex--;
            if (playerIndex < 0) {
                playerIndex = teamData.size() - 1;
            }
            update(event);
        }
        idleTimerThread.interrupt();
    }

    private void update(ButtonInteractionEvent event) {
        event.edit(InteractionApplicationCommandCallbackSpec.create()
                .withEmbeds(getPlayerEmbed(matchData, teamData.get(playerIndex)))).block();
    }

    protected void startIdleTimer() {
        try {
            Thread.sleep(TimeUnit.MINUTES.toMillis(3));
            message.edit(MessageEditSpec.create()
                    .withComponents()).block();
        } catch (InterruptedException e) {
            if (active) {
                startIdleTimer();
            } else {
                Thread.currentThread().interrupt();
            }
        }
    }

    private EmbedCreateSpec getPlayerEmbed(JSONObject matchData, JSONObject pData) {
        EmbedCreateSpec embed = EmbedCreateSpec.create()
                .withThumbnail("https://raw.communitydragon.org/latest/plugins/rcp-be-lol-game-data/global/default/v1/champion-icons/"
                        + pData.getInt("championId") + ".png")
                .withAuthor(EmbedCreateFields.Author.of(pData.getString("summonerName"), "",
                        "https://raw.communitydragon.org/latest/plugins/rcp-be-lol-game-data/global/default/v1/profile-icons/"
                                + pData.getInt("profileIcon") + ".jpg"))
                .withColor(pData.getBoolean("win") ? DiscordColor.GREEN : DiscordColor.RED)
                .withTimestamp(Instant.ofEpochMilli(matchData.getLong("gameStartTimestamp")))
                .withFooter(EmbedCreateFields.Footer.of(getDurationString(matchData.has("gameEndTimestamp")
                        ? matchData.getLong("gameDuration") : matchData.getLong("gameDuration") / 1000), ""));

        ArrayList<EmbedCreateFields.Field> fields = new ArrayList<>();

        fields.add(EmbedCreateFields.Field.of("Queue ☄️", LeagueCommand.queueMap.get(matchData.getInt("queueId")), true));
        fields.add(EmbedCreateFields.Field.of("Result 📖", (pData.getBoolean("win") ? "Victory" : "Defeat")
                + ((pData.getBoolean("gameEndedInSurrender") || pData.getBoolean("gameEndedInEarlySurrender")) ? " (Surr)" : ""), true));

        fields.add(EmbedCreateFields.Field.of("Champion 👤", pData.getString("championName"), true));

        if (!pData.getString("teamPosition").isEmpty()) {
            String teamPosition = pData.getString("teamPosition");
            fields.add(EmbedCreateFields.Field.of("Position 👣",
                    teamPosition.equals("UTILITY") ? "Support" : capitalize(teamPosition), true)); //little hacky
        }

        fields.add(EmbedCreateFields.Field.of("K / D / A ⚔️", pData.getInt("kills")
                + " / " + pData.getInt("deaths") + " / " + pData.getInt("assists"), true));
        fields.add(EmbedCreateFields.Field.of("Kill Particip. 🗡️", getKillParticipation(pData, teamData), true));
        fields.add(EmbedCreateFields.Field.of("Damage Dealt 🩸",
                String.valueOf(pData.getInt("totalDamageDealtToChampions")), true));

        if (pData.getInt("doubleKills") > 0) {
            fields.add(EmbedCreateFields.Field.of("Double Kills 👨‍👦", String.valueOf(pData.getInt("doubleKills")), true));
        }
        if (pData.getInt("tripleKills") > 0) {
            fields.add(EmbedCreateFields.Field.of("Triple Kills 👨‍👨‍👦", String.valueOf(pData.getInt("doubleKills")), true));
        }
        if (pData.getInt("quadraKills") > 0) {
            fields.add(EmbedCreateFields.Field.of("Quadra Kills 👨‍👨‍👦‍👦", String.valueOf(pData.getInt("quadraKills")), true));
        }
        if (pData.getInt("pentaKills") > 0) {
            fields.add(EmbedCreateFields.Field.of("Penta Kills 👨‍👨‍👦 👨‍👦", String.valueOf(pData.getInt("pentaKills")), true));
        }
        if (pData.getInt("largestKillingSpree") > 0) {
            fields.add(EmbedCreateFields.Field.of("Largest Spree 🔥", pData.getInt("largestKillingSpree") + " Kills", true));
        }

        fields.add(EmbedCreateFields.Field.of("CS 🎯", getCsString(matchData, pData), true));
        fields.add(EmbedCreateFields.Field.of("Gold Earned 🪙", String.valueOf(pData.getInt("goldEarned")), true));

        if (pData.getInt("visionScore") > 0) {
            fields.add(EmbedCreateFields.Field.of("Vision Score 👁️", String.valueOf(pData.getInt("visionScore")), true));
        }

        return embed.withFields(fields);
    }

    private static String capitalize(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1).toLowerCase();
    }

    private static String getDurationString(long seconds) {
        String duration = "";

        if (seconds >= 60) {
            duration += seconds / 60 + "m ";
            seconds = seconds % 60;
        }

        if (seconds > 0) {
            duration += seconds + "s";
        }

        return duration;
    }

    private static String getCsString(JSONObject matchData, JSONObject pData) {
        int cs = pData.getInt("totalMinionsKilled") + pData.getInt("neutralMinionsKilled");
        long length = matchData.has("gameEndTimestamp")
                ? matchData.getLong("gameDuration") : matchData.getLong("gameDuration") / 1000;
        return cs + String.format(" (%.1f/min)", 60.0 / length * cs);
    }

    private static String getKillParticipation(JSONObject pData, ArrayList<JSONObject> teamData) {
        int teamKills = 0;
        for (JSONObject player : teamData) {
            teamKills += player.getInt("kills");
        }
        return Math.round((pData.getInt("kills") + pData.getInt("assists")) / (double) teamKills * 100) + "%";
    }

}
