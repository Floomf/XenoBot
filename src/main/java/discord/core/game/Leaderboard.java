package discord.core.game;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import discord4j.core.object.entity.User;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Leaderboard {

    private final String file;
    public HashMap<Long, Integer> highScoresMap;

    public Leaderboard(String file) {
        this.file = file;
        try {
            this.highScoresMap = new ObjectMapper().readValue(new File(file), new TypeReference<>() {});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean submitScore(User user, int score) {
        if (!highScoresMap.containsKey(user.getId().asLong()) || score > highScoresMap.get(user.getId().asLong())) {
            highScoresMap.put(user.getId().asLong(), score);
            saveScores();
            return true;
        }
        return false;
    }

    @Override
    public String toString() { //don't sort it until we actually need to display it
        StringBuilder sb = new StringBuilder();
        List<Map.Entry<Long, Integer>> sortedScores = new ArrayList<>(highScoresMap.entrySet());
        sortedScores.sort((entry1, entry2) -> entry2.getValue() - entry1.getValue());

        int currPlace = 1;
        int lastScore = -1;
        for (Map.Entry<Long, Integer> score : sortedScores) {
            if (currPlace > 10) { //Only show top 10
                break;
            }

            if (score.getValue() == lastScore) {
                sb.append(", <@!").append(score.getKey()).append(">");
            } else {
                sb.append("\n").append(getFormattedPlace(currPlace))
                        .append(" `").append(score.getValue())
                        .append("` ― <@!").append(score.getKey()).append(">");
                currPlace++;
            }
            lastScore = score.getValue();
        }
        return sb.toString();
    }

    private static String getFormattedPlace(int place) {
        if (place == 1) return "🥇 ";
        else if (place == 2) return "🥈 ";
        else if (place == 3) return "🥉 ";
        else return "**" + place + ")** ";
        //else return "**" + ProfileBuilder.getOrdinal(place) + ")**";
    }

    public void saveScores() {
        try {
            new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(new File(file), highScoresMap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
