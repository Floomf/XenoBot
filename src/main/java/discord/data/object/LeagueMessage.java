package discord.data.object;

import discord.command.fun.LeagueCommand;
import discord.core.command.InteractionContext;
import discord.data.object.user.DUser;
import discord.util.DiscordColor;
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

    private static final String TEAM_BEST_SYMBOL = "⭐";
    private static final String GAME_BEST_SYMBOL = "👑";

    protected final Message message;
    protected final Thread idleTimerThread;

    protected final String matchId;
    protected final JSONObject matchData;
    protected ArrayList<JSONObject> teamData;
    protected ArrayList<JSONObject> enemyData;

    protected boolean active;

    private final DUser dUser;
    private int playerIndex;

    public LeagueMessage(InteractionContext context, String matchId, JSONObject matchData, JSONObject pData,
                         ArrayList<JSONObject> teamData, ArrayList<JSONObject> enemyData) {
        dUser = context.getDUser();

        for (int i = 0; i < teamData.size(); i++) {
            if (teamData.get(i).getString("summonerName").equals(pData.getString("summonerName"))) {
                playerIndex = i;
                break;
            }
        }

        this.matchId = matchId.substring(4);
        this.matchData = matchData;
        this.teamData = teamData;
        this.enemyData = enemyData;

        context.event.editReply(InteractionReplyEditSpec.create()
                .withEmbeds(getPlayerEmbed(matchData, pData))
                .withComponents(ActionRow.of(Button.secondary("previous", "Previous Teammate"),
                        Button.secondary("next", "Next Teammate"), Button.primary("switch", "Switch Teams")))).block();

        this.message = context.getChannel().getMessageById(
                Snowflake.of(context.event.getInteractionResponse().getInitialResponse().block().id())).block();
        this.idleTimerThread = new Thread(this::startIdleTimer);
        this.active = true;
        idleTimerThread.start();

        context.getChannel().getClient().on(ButtonInteractionEvent.class)
                .takeUntil(p -> !active)
                .filter(event -> event.getMessageId().equals(message.getId()))
                .filter(event -> event.getInteraction().getUser().getId().asLong() == dUser.getDiscordID())
                .subscribe(this::onButton);
    }

    private void onButton(ButtonInteractionEvent event) {
        if (event.getCustomId().equals("next")) {
            playerIndex++;
            if (playerIndex == teamData.size()) {
                playerIndex = 0;
            }
        } else if (event.getCustomId().equals("previous")) {
            playerIndex--;
            if (playerIndex < 0) {
                playerIndex = teamData.size() - 1;
            }
        } else if (event.getCustomId().equals("switch")) {
            ArrayList<JSONObject> temp = teamData;
            teamData = enemyData;
            enemyData = temp;
        }
        update(event);
        idleTimerThread.interrupt();
    }

    private void update(ButtonInteractionEvent event) {
        event.edit(InteractionApplicationCommandCallbackSpec.create()
                .withEmbeds(getPlayerEmbed(matchData, teamData.get(playerIndex)))).block();
    }

    protected void startIdleTimer() {
        try {
            Thread.sleep(TimeUnit.MINUTES.toMillis(5));
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
                .withFooter(EmbedCreateFields.Footer.of("(" + TEAM_BEST_SYMBOL + " = Team Best, " + GAME_BEST_SYMBOL + " = Game Best)\n"
                        + "(ID: " + matchId + ")  •  " + getDurationString(matchData.has("gameEndTimestamp")
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

        fields.add(EmbedCreateFields.Field.of("K / D / A ⚔️", getKDA(pData), true));
        fields.add(EmbedCreateFields.Field.of("Kill Particip. 🗡️", getKillParticipation(pData), true));
        fields.add(EmbedCreateFields.Field.of("Damage Dealt 🩸", getStat(pData,"totalDamageDealtToChampions"), true));

        if (pData.getInt("doubleKills") > 0) {
            fields.add(EmbedCreateFields.Field.of("Double Kills 👨‍👦", getStat(pData,"doubleKills"), true));
        }
        if (pData.getInt("tripleKills") > 0) {
            fields.add(EmbedCreateFields.Field.of("Triple Kills 👨‍👨‍👦", getStat(pData,"tripleKills"), true));
        }
        if (pData.getInt("quadraKills") > 0) {
            fields.add(EmbedCreateFields.Field.of("Quadra Kills 👨‍👨‍👦‍👦", getStat(pData,"quadraKills"), true));
        }
        if (pData.getInt("pentaKills") > 0) {
            fields.add(EmbedCreateFields.Field.of("Penta Kills 👨‍👨‍👦 👨‍👦", getStat(pData,"pentaKills"), true));
        }
        if (pData.getInt("largestKillingSpree") > 0) {
            fields.add(EmbedCreateFields.Field.of("Largest Spree 🔥", pData.getInt("largestKillingSpree") + " Kills"
                    + getPossibleStar(pData, "largestKillingSpree"), true));
        }

        fields.add(EmbedCreateFields.Field.of("CS 🎯", getCsString(matchData, pData), true));
        fields.add(EmbedCreateFields.Field.of("Gold Earned 🪙", getStat(pData,"goldEarned"), true));

        if (pData.getInt("visionScore") > 0) {
            fields.add(EmbedCreateFields.Field.of("Vision Score 👁️", getStat(pData,"visionScore"), true));
        }

        fields.add(EmbedCreateFields.Field.of("Item Build ⚒️", getItemBuild(pData), false));

        return embed.withFields(fields);
    }

    private String getStat(JSONObject pData, String property) {
        return pData.getInt(property) + getPossibleStar(pData, property);
    }

    public String getPossibleStar(JSONObject pData, String property) {
        if (getMaxPropertyForTeam(teamData, property) == pData.getInt(property)) {
            if (getMaxPropertyForTeam(enemyData, property) <= pData.getInt(property)) {
                return " " + GAME_BEST_SYMBOL;
            } else {
                return " " + TEAM_BEST_SYMBOL;
            }
        }
        return "";
    }

    private int getMaxPropertyForTeam(ArrayList<JSONObject> teamData, String property) {
        return max(teamData.get(0).getInt(property), teamData.get(1).getInt(property), teamData.get(2).getInt(property),
                teamData.get(3).getInt(property), teamData.get(4).getInt(property));
    }

    private static int max(int num1, int num2, int num3, int num4, int num5) {
        return Math.max(num1, Math.max(num2, Math.max(num3, Math.max(num4, num5))));
    }

    private static double max(double num1, double num2, double num3, double num4, double num5) {
        return Math.max(num1, Math.max(num2, Math.max(num3, Math.max(num4, num5))));
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

    private int getCs(JSONObject pData) {
        return pData.getInt("totalMinionsKilled") + pData.getInt("neutralMinionsKilled");
    }

    private String getCsString(JSONObject matchData, JSONObject pData) {
        String csString = "";
        int cs = getCs(pData);
        csString += cs;
        long length = matchData.has("gameEndTimestamp")
                ? matchData.getLong("gameDuration") : matchData.getLong("gameDuration") / 1000;
        csString += String.format(" (%.1f/min)", 60.0 / length * cs);

        if (max(getCs(teamData.get(0)), getCs(teamData.get(1)), getCs(teamData.get(2)),
                getCs(teamData.get(3)), getCs(teamData.get(4))) == cs) {
            csString += " ";
            if (max(getCs(enemyData.get(0)), getCs(enemyData.get(1)), getCs(enemyData.get(2)),
                    getCs(enemyData.get(3)), getCs(enemyData.get(4))) <= cs) {
                csString += GAME_BEST_SYMBOL;
            } else {
                csString += TEAM_BEST_SYMBOL;
            }
        }
        return csString;
    }

    private int getKillsAndAssists(JSONObject pData) {
        return pData.getInt("kills") + pData.getInt("assists");
    }

    private double getKDARatio(JSONObject pData) {
        return getKillsAndAssists(pData) / ((double) pData.getInt("deaths"));
    }

    private String getKDA(JSONObject pData) {
        String kdaString = pData.getInt("kills") + "/" + pData.getInt("deaths")
                + "/" + pData.getInt("assists") + String.format(" (%.1f)", getKDARatio(pData));
        if (max(getKDARatio(teamData.get(0)), getKDARatio(teamData.get(1)), getKDARatio(teamData.get(2)),
                getKDARatio(teamData.get(3)), getKDARatio(teamData.get(4))) == getKDARatio(pData)) {
            kdaString += " ";
            if (max(getKDARatio(enemyData.get(0)), getKDARatio(enemyData.get(1)), getKDARatio(enemyData.get(2)),
                    getKDARatio(enemyData.get(3)), getKDARatio(enemyData.get(4))) <= getKDARatio(pData)) {
                kdaString += GAME_BEST_SYMBOL;
            } else {
                kdaString += TEAM_BEST_SYMBOL;
            }
        }
        return kdaString;
    }

    private int getKillParticipation(JSONObject pData, ArrayList<JSONObject> teamData) {
        int teamKills = 0;
        for (JSONObject player : teamData) {
            teamKills += player.getInt("kills");
        }

        return (int) Math.round(getKillsAndAssists(pData) / (double) teamKills * 100);
    }

    private String getKillParticipation(JSONObject pData) {
        String kpString = getKillParticipation(pData, teamData) + "%";

        if (max(getKillsAndAssists(teamData.get(0)), getKillsAndAssists(teamData.get(1)), getKillsAndAssists(teamData.get(2)),
                getKillsAndAssists(teamData.get(3)), getKillsAndAssists(teamData.get(4))) == getKillsAndAssists(pData)) {
            kpString += " ";
            //we have to use kill participation method instead here because percentage is based on total team kills
            if (max(getKillParticipation(enemyData.get(0), enemyData), getKillParticipation(enemyData.get(1), enemyData),
                    getKillParticipation(enemyData.get(2), enemyData), getKillParticipation(enemyData.get(4), enemyData),
                    getKillParticipation(enemyData.get(0), enemyData)) <= getKillParticipation(pData, teamData)) {
                kpString += GAME_BEST_SYMBOL;
            } else {
                kpString += TEAM_BEST_SYMBOL;
            }
        }
        return kpString;
    }

    private String getItemBuild(JSONObject pData) {
        String build = "";
        for (int i = 0; i < 6; i++) {
            build += LeagueCommand.ITEM_MAP.get(pData.getInt("item" + i)) + "\n";
        }
        return build;
    }

}
