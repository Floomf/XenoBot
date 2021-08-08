package discord.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import discord.data.object.Poll;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class PollManager {

    private static final HashMap<Long, Poll> POLL_MAP = new HashMap<>();

    public static void loadPolls(GatewayDiscordClient client) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            System.out.println("Loading polls...");
            Poll.SerializableData[] pollData = mapper.readValue(new File("polls.json"),
                    Poll.SerializableData[].class);
            for (Poll.SerializableData data : pollData) {
                Poll poll = new Poll(data, client);
                POLL_MAP.put(data.ownerID, poll);
                poll.start();
            }
            System.out.println(pollData.length + " poll(s) loaded.");
        } catch (IOException e) {
            System.err.println("Polls failed to load with error: " + e);
        }
    }

    public static void savePolls() {
        ObjectMapper mapper = new ObjectMapper();
        System.out.println("Attempting to save polls...");
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File("polls.json"),
                    POLL_MAP.values().stream().map(Poll::getSerializableData).toArray(Poll.SerializableData[]::new));
            System.out.println("Polls saved.");
        } catch (IOException e) {
            System.err.println("Polls failed to save with error: " + e);
        }
    }

    public static void addPoll(User user, Poll poll) {
        POLL_MAP.put(user.getId().asLong(), poll);
    }

    public static void removePoll(User user) {
        POLL_MAP.remove(user.getId().asLong());
    }

    public static boolean hasPoll(User user) {
        return POLL_MAP.containsKey(user.getId().asLong());
    }

}
