package discord.command.game.hangman;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import discord.core.game.SingleplayerGame;
import discord.util.BotUtils;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class GameHangman extends SingleplayerGame {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static HashMap<Long, Integer> WIN_STREAKS = new HashMap<>();

    private String word;
    private String partOfSpeech;

    private char[] wordProgress;
    private char[] guesses;

    private int missesLeft;

    private final List<Snowflake> inputMessages = new ArrayList<>();

    static {
        try {
            WIN_STREAKS = mapper.readValue(new File("streaks_hangman.json"), new TypeReference<HashMap<Long, Integer>>() {});
        } catch (IOException e) {
            LoggerFactory.getLogger(GameHangman.class).error(e.toString());
        }
    }

    public GameHangman(String gameTitle, TextChannel channel, Member player, int betAmount) {
        super(gameTitle, channel, player, 0);
    }

    private static void saveStreaks() {
        try {
            mapper.writeValue(new File("streaks_hangman.json"), WIN_STREAKS);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected String getForfeitMessage() {
        return "**You forfeited.**";
    }

    @Override
    protected String getIdleMessage() {
        return "**You failed to guess in time.** The word was:\n\n" + getFullWordAndInfo();
    }

    @Override
    protected void setup() {
        int length = getRandIntInRange(4, 30);
        assignRandomWord(length);
        wordProgress = new char[word.length()];
        guesses = new char[26];
        Arrays.fill(guesses, '-');

        for (int i = 0; i < wordProgress.length; i++) {
            if (word.charAt(i) < 'A' || word.charAt(i) > 'z') {
                wordProgress[i] = word.charAt(i);
            } else {
                wordProgress[i] = '_';
            }
        }
        if (length <= 15) { //4-7, 8-11, 12-15
            missesLeft = 9 - (length / 4);
        } else { //16-20, 21-25, 26-30
            missesLeft = 8 - ((length - 1) / 5);
        }
    }

    @Override
    protected String getFirstDisplay() {
        return "**Start!** Guess a letter:\n\n" + getWordGuessAndInfo();
    }

    @Override
    protected void onStart() {
        super.registerMessageListener();
    }

    @Override
    protected void onEnd() {
        getChannel().bulkDelete(Mono.just(inputMessages).flatMapMany(Flux::fromIterable)).blockFirst();
        saveStreaks();
    }

    @Override
    protected void onTimeout() {
        WIN_STREAKS.remove(getPlayer().getId().asLong());
        super.onTimeout();
    }

    private static int getRandIntInRange(int min, int max) {
        return new Random().nextInt(max - min + 1) + min;
    }

    private static String getPOSFromLetter(String letter) {
        switch (letter) {
            case "n":
                return "Noun, ";
            case "v":
                return "Verb, ";
            case "adj":
                return "Adjective, ";
            case "adv":
                return "Adverb, ";
            default:
                return "";
        }
    }

    private void assignRandomWord(int length) {
        Random r = new Random();
        String firstLetters = "abcdefghijklmnopqrstuwy";
        String lettersToAdd = "????????????????????????????????"; //up to 33 letters total
        String url = "https://api.datamuse.com/words?md=p&sp=";
        url += firstLetters.charAt(r.nextInt(firstLetters.length()));
        url += lettersToAdd.substring(0, length - 1);

        JSONArray wordsJson = Unirest.get(url).asJson().getBody().getArray();

        if (wordsJson.isEmpty()) {
            assignRandomWord(length);
            return;
        }

        JSONObject wordJson = wordsJson.getJSONObject(r.nextInt(wordsJson.length()));

        if (wordJson.isNull("word")) {
            assignRandomWord(length);
            return;
        }

        word = wordJson.getString("word");

        if (wordJson.isNull("tags")) {
            partOfSpeech = "";
        } else {
            partOfSpeech = getPOSFromLetter(wordJson.getJSONArray("tags").getString(0));
        }
    }


    @Override
    protected void onTurn(String input) {
        guesses[input.charAt(0) - 'a'] = input.charAt(0);
        if (word.contains(input)) {
            //fill in guessing
            for (int i = 0; i < word.length(); i++) {
                if (word.charAt(i) == input.charAt(0)) {
                    wordProgress[i] = input.toUpperCase().charAt(0);
                }
            }
            if (hasWon()) {
                if (WIN_STREAKS.containsKey(getPlayer().getId().asLong())) {
                    int streak = WIN_STREAKS.get(getPlayer().getId().asLong());
                    WIN_STREAKS.put(getPlayer().getId().asLong(), streak + 1);
                    win("🎉 **You win!** The answer was:\n\n" + getFullWordAndInfo()
                                    + "\n\n📈 **" + (1 + streak) + "** win streak!\n💰 **$" + (streak * 5) + "** bonus!",
                            65 + (streak * 5));
                } else {
                    WIN_STREAKS.put(getPlayer().getId().asLong(), 1);
                    win("🎉 **You win!** The answer was:\n\n" + getFullWordAndInfo(), 65);
                }
            } else {
                super.setInfoDisplay("✅ **Yep!** Guess again:");
            }
        } else {
            missesLeft--;
            if (missesLeft == 0) {
                if (WIN_STREAKS.containsKey(getPlayer().getId().asLong()) && WIN_STREAKS.get(getPlayer().getId().asLong()) > 1) {
                    WIN_STREAKS.remove(getPlayer().getId().asLong());
                    lose(BotUtils.getRandomGuildEmoji(super.getGameMessage().getGuild().block(),
                            new String[] {"Sadge", "PepeHands"})
                            + " **You lose. Win streak ended.**\nThe answer was:\n\n" + getFullWordAndInfo());
                } else {
                    WIN_STREAKS.remove(getPlayer().getId().asLong());
                    lose(BotUtils.getRandomGuildEmoji(super.getGameMessage().getGuild().block(),
                            new String[] {"Sadge", "PepeHands"})
                            + " **You lose.** The answer was:\n\n" + getFullWordAndInfo());
                }
            } else {
                super.setInfoDisplay("❌ **Nope!** Guess again:");
            }
        }
        if (inputMessages.size() >= 3) {
            getChannel().bulkDelete(Mono.just(inputMessages).flatMapMany(Flux::fromIterable)).blockFirst();
            inputMessages.clear();
        }
    }

    @Override
    protected final void onPlayerMessage(Message message, Member player) {
        inputMessages.add(message.getId());
        super.onPlayerMessage(message, player);
    }

    @Override
    protected boolean isValidInput(String input) {
        return input.matches("[a-z]") && !new String(guesses).contains(input);
    }

    @Override
    protected String getBoard() {
        return getWordGuessAndInfo()
                + "\n\n💬 Misses left: `" + missesLeft + "`"
                + "\n🔠 **Guessed:**\n`" + new String(guesses).toUpperCase() + "`";
    }

    private boolean hasWon() {
        for (char c : wordProgress) {
            if (c == '_') return false;
        }
        return true;
    }

    private String getFullWordAndInfo() {
        return "```yaml\n" + word.toUpperCase() + "```\n" + getWordInfo();
    }

    private String getWordGuessAndInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("```yaml\n");
        for (char c : wordProgress) {
            sb.append(c).append(" ");
        }
        sb.append("```\n").append(getWordInfo());
        return sb.toString();
    }

    private String getWordInfo() {
        return "*(" + partOfSpeech + getLetterCount() + " Letters)*";
    }

    private int getLetterCount() {
        return word.replace("-", "").replace(" ", "").length();
    }

}
