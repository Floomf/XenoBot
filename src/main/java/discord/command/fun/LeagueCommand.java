package discord.command.fun;

import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.core.command.InteractionContext;
import discord.data.object.LeagueMessage;
import discord.util.BotUtils;
import discord.util.MessageUtils;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.spec.InteractionReplyEditSpec;
import discord4j.discordjson.json.ApplicationCommandInteractionOptionData;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;

public class LeagueCommand extends AbstractCommand {

    public static final HashMap<Integer, String> queueMap = new HashMap<>();

    public LeagueCommand() {
        super("lol", 0, CommandCategory.FUN);
        setupQueueMap();
    }

    private void setupQueueMap() {
        JSONArray queues = Unirest.get("https://static.developer.riotgames.com/docs/lol/queues.json").asJson().getBody().getArray();
        queueMap.put(0, "Custom game"); //hardcoded trick
        for (int i = 1; i < queues.length(); i++) {
            JSONObject queue = queues.getJSONObject(i);
            queueMap.put(queue.getInt("queueId"), queue.getString("description")
                    .replace("5v5", "").replace("games", "").trim());
        }
    }

    @Override
    public ApplicationCommandRequest buildSlashCommand() {
        return ApplicationCommandRequest.builder()
                .name(getName())
                .description("View info about a user's latest match of League of Legends")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("summoner")
                        .description("Summoner Name (NA)")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .required(true)
                        .build())
                .addOption(ApplicationCommandOptionData.builder()
                        .name("match_id")
                        .description("A particular match id (instead of fetching latest match)")
                        .type(ApplicationCommandOption.Type.INTEGER.getValue())
                        .required(false)
                        .build())
                .build();
    }

    @Override
    public void execute(InteractionContext context) {
        context.acknowledge();
        HttpResponse<JsonNode> summonerResponse =
                Unirest.get("https://na1.api.riotgames.com/lol/summoner/v4/summoners/by-name/" + context.getOptionAsString("summoner"))
                .header("X-Riot-Token", "RGAPI-164fc3ab-4c82-4a2b-8a10-a78367e14b8e")
                .asJson();

        if (summonerResponse.getStatus() == 404) {
            context.editReply(MessageUtils.getNewErrorEmbed("Couldn't fetch a summoner by that name. Did you type it in correctly?"));
            return;
        }

        if (!summonerResponse.getBody().getObject().has("puuid")) {
            context.event.editReply(InteractionReplyEditSpec.create()
                    .withEmbeds(MessageUtils.getNewErrorEmbed("API key expired. Tell the admin to fix it."))).block();
            return;
        }

        String puuid = summonerResponse.getBody().getObject().getString("puuid");

        String matchId = context.getOptionAsLong("match_id").map(id -> "NA1_" + id).orElse("");
        if (matchId.isEmpty()) {
            JSONArray latestMatches = Unirest.get("https://americas.api.riotgames.com/lol/match/v5/matches/by-puuid/" + puuid + "/ids?count=1")
                    .header("X-Riot-Token", "RGAPI-164fc3ab-4c82-4a2b-8a10-a78367e14b8e")
                    .asJson().getBody().getArray();

            if (latestMatches.isEmpty()) {
                context.editReply(MessageUtils.getNewErrorEmbed("Couldn't find the latest match played by "
                        + context.getOptionAsString("summoner") + ".\n\nHave they played the game recently?"));
                return;
            }
            matchId = latestMatches.getString(0);
        }

        HttpResponse<JsonNode> matchResponse = Unirest.get("https://americas.api.riotgames.com/lol/match/v5/matches/" + matchId)
            .header("X-Riot-Token", "RGAPI-164fc3ab-4c82-4a2b-8a10-a78367e14b8e")
            .asJson();

        if (matchResponse.getStatus() == 404) {
            context.editReply(MessageUtils.getNewErrorEmbed("Couldn't fetch a match by that ID. Did you type it in correctly?"));
            return;
        }

        JSONObject matchData = matchResponse.getBody().getObject().getJSONObject("info");

        ArrayList<JSONObject> participants = (ArrayList<JSONObject>) matchData.getJSONArray("participants").toList();
        //if they enter in a random player but a valid match id, we just ignore the player
        JSONObject pData = participants.stream().filter(o -> o.getString("puuid").equals(puuid)).findFirst().orElse(participants.get(0));

        ArrayList<JSONObject> enemyData = participants.stream().filter(json -> json.getInt("teamId") != pData.getInt("teamId"))
                .collect(Collectors.toCollection(ArrayList::new));

        participants.removeIf(json -> json.getInt("teamId") != pData.getInt("teamId")); //only teammates

        new LeagueMessage(context, matchId, matchData, pData, participants, enemyData);
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "", "");
    }

    @Override
    public boolean isSupportedGlobally() {
        return true;
    }

}
