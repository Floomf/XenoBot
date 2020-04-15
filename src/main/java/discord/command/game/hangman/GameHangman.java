package discord.command.game.hangman;

import discord.core.game.TypeGame;
import discord.data.UserManager;
import discord.data.object.user.DUser;
import discord.util.BotUtils;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;

import java.util.Arrays;
import java.util.Random;

public class GameHangman extends TypeGame {

    private String word;
    private String partOfSpeech;

    private AnswerType type;

    private char[] wordProgress;
    private char[] guesses;

    private int triesLeft;

    public enum AnswerType {
        SMALL, MEDIUM, LARGE, HUGE
    }

    public GameHangman(Message message, Member[] players, AnswerType type) {
        super(message, players);

        assignRandomWord(type);
        wordProgress = new char[word.length()];
        guesses = new char[26];
        this.type = type;
        Arrays.fill(guesses, '-');

        for (int i = 0; i < wordProgress.length; i++) {
            if (word.charAt(i) == '-' || word.charAt(i) == ' ') {
                wordProgress[i] = word.charAt(i);
            } else {
                wordProgress[i] = '_';
            }
        }
        for (int i = 0; i < AnswerType.values().length; i++) {
            if (type == AnswerType.values()[i]) {
                triesLeft = 9 - i;
            }
        }
    }

    private static int getRandIntInRange(int min, int max) {
        return new Random().nextInt(max - min + 1) + min;
    }

    private static int getLettersFromType(AnswerType type) {
        switch (type) {
            case SMALL:
                return getRandIntInRange(3, 5);
            case MEDIUM:
                return getRandIntInRange(6, 12);
            case LARGE:
                return getRandIntInRange(13, 20);
            case HUGE:
                return getRandIntInRange(21, 33);
        }
        return 0; //bad
    }

    private static String getPOSFromLetter(String letter) {
        switch (letter) {
            case "n":
                return "Noun";
            case "v":
                return "Verb";
            case "adj":
                return "Adjective";
            case "adv":
                return "Adverb";
            default:
                return "Unknown";
        }
    }

    private void assignRandomWord(AnswerType type) {
        Random r = new Random();
        String firstLetters = "abcdefghijklmnopqrstuwy";
        String lettersToAdd = "????????????????????????????????"; //up to 33 letters total
        String url = "https://api.datamuse.com/words?md=p&sp=";
        url += firstLetters.charAt(r.nextInt(firstLetters.length()));
        url += lettersToAdd.substring(0, getLettersFromType(type) - 1);

        JSONArray wordsJson = Unirest.get(url).asJson().getBody().getArray();
        JSONObject wordJson = wordsJson.getJSONObject(r.nextInt(wordsJson.length()));

        word = wordJson.getString("word");
        System.out.println("Fetched " + word);

        if (wordJson.isNull("tags")) {
            partOfSpeech = "???";
        } else {
            partOfSpeech = getPOSFromLetter(wordJson.getJSONArray("tags").getString(0));
        }
    }

    @Override
    protected String getGameTitle() {
        return "Hangman";
    }

    @Override
    protected String getForfeitMessage(Member forfeiter) {
        return "**You forfeited.**";
    }

    @Override
    protected String getIdleMessage(Member idler) {
        return "**You failed to guess in time.** The word was:\n\n" + getFullWordAndInfo();
    }

    @Override
    protected void onStart() {
        super.setGameDisplay("**Start!** Guess a letter:\n\n" + getWordGuessAndInfo());
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
                DUser user = UserManager.getDUserFromMember(super.getPlayerThisTurn());
                if (user.getProg().isNotMaxLevel()) {
                    user.getProg().addXP(getXPForAnswerType());
                    win("🎉 **You win!** The answer was:\n\n" + getFullWordAndInfo()
                            + "\n\n`+" + getXPForAnswerType() + " XP` " + BotUtils.getGuildEmojiString(super.getGameMessage().getGuild().block(), "Pog"));
                } else {
                    win("🎉 **You win!** The answer was:\n\n" + getFullWordAndInfo());
                }
            } else {
                super.setInfoDisplay("✅ **Yep!** Guess again:");
            }
        } else {
            triesLeft--;
            if (triesLeft == 0) {
                win(BotUtils.getGuildEmojiString(super.getGameMessage().getGuild().block(), "PepeHands")
                        + " **You lose.** The answer was:\n\n" + getFullWordAndInfo());
            } else {
                super.setInfoDisplay("❌ **Nope!** Guess again:");
            }
        }
    }

    @Override
    protected boolean isValidInput(String input) {
        return input.matches("[a-z]") && !new String(guesses).contains(input);
    }

    @Override
    protected String getBoard() {
        return getWordGuessAndInfo()
                + "\n\n💬 Tries left: `" + triesLeft + "`"
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
        return "*(" + partOfSpeech + ", " + getLetterCount() + " Letters)*";
    }

    private int getLetterCount() {
        return word.replace("-", "").replace(" ", "").length();
    }

    private int getXPForAnswerType() {
        int index = 4;
        for (AnswerType type : AnswerType.values()) {
            if (this.type.equals(type)) {
                return index * 5;
            }
            index--;
        }
        return 0;
    }

}
