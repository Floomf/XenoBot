package discord.command.game.wordle;

import discord.core.game.GameEmoji;
import discord.core.game.SingleplayerGame;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.spec.MessageEditSpec;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import java.util.Random;

public class GameWordle extends SingleplayerGame {

    private Message infoMessage;

    private String word;
    private boolean[] foundLetters;

    private String lastGuess;
    private char[] lettersAvailable;

    private int missesLeft;

    private String guessesDisplay;

    public GameWordle(String gameTitle, TextChannel channel, Member player, int betAmount) {
        super(gameTitle, channel, player, 0);
    }

    @Override
    protected boolean useEmbed() {
        return false;
    }

    @Override
    protected String getForfeitMessage() {
        return "**You forfeited.**";
    }

    @Override
    protected String getIdleMessage() {
        return "You failed to go in time. The word was:\n\n" + GameEmoji.wordToLetterEmojis(word);
    }

    @Override
    protected void setup() {
        word = getRandomWord(5);
        foundLetters = new boolean[word.length()];
        lastGuess = "_____";
        guessesDisplay = "";
        missesLeft = 6;
        System.out.println(word);

        lettersAvailable = new char[26];
        for (int c = 0; c < 26; c++) { //fill up lettersAvailable with all letters
            lettersAvailable[c] = (char) (c + 'a');
        }

    }

    static String getRandomWord(int length) {
        Random r = new Random();
        String firstLetters = "abcdefghijklmnopqrstuwy";
        String lettersToAdd = "????????????????????????????????"; //up to 33 letters total
        String url = "https://api.datamuse.com/words?md=p&max=15&sp=";
        url += firstLetters.charAt(r.nextInt(firstLetters.length()));
        url += lettersToAdd.substring(0, length - 1);

        JSONArray wordsJson = Unirest.get(url).asJson().getBody().getArray();

        if (wordsJson.isEmpty()) {
            return getRandomWord(length);
        }

        JSONObject wordJson = wordsJson.getJSONObject(r.nextInt(wordsJson.length()));

        if (wordJson.isNull("word")) {
            return getRandomWord(length);
        }

        return wordJson.getString("word");
    }

    @Override
    protected String getFirstDisplay() {
        return "🟦 🟦 🟦 🟦 🟦";
    }

    @Override
    protected void onStart() {
        super.registerMessageListener();
        infoMessage = getChannel().createMessage("Guess the correct word within 6 tries.\n" //putting this here for now
                + "After each guess, symbols will appear below the word to show how close your guess was.\n\n"
                + GameEmoji.CHECKMARK + " - Letter is in the correct spot\n"
                + GameEmoji.DOUBLE_ARROW + " - Letter is in the word but wrong spot\n"
                + GameEmoji.X + " - Letter is not in the word\n\n**Begin by typing a 5 letter word!**").block();
    }

    @Override
    protected void onTurn(String input) {
        for (int i = 0; i < input.length(); i++) {
            if (!foundLetters[i] && input.charAt(i) == word.charAt(i)) {
                foundLetters[i] = true;
            } else if (!word.contains(String.valueOf(input.charAt(i)))) {
                lettersAvailable[input.charAt(i) - 'a'] = '-';
            }
        }
        lastGuess = input;
        addToBoard();

        if (input.equals(word)) {
            //win
            infoMessage.delete().block(); //temp

            super.win(getBoard() + "\n\n**You win!**", 75);
        } else {
            missesLeft--;
            super.setInfoDisplay("");

            if (missesLeft == 0) {
                infoMessage.delete().block();//temp
                super.lose("You lose! The word was:\n\n" + GameEmoji.wordToLetterEmojis(word));
            } else {
                updateInfo("\uD83D\uDCAC **Misses left:** " + missesLeft);
            }
        }

    }

    private void updateInfo(String message) {
        infoMessage.edit(MessageEditSpec.create().withContentOrNull(message + "\n\uD83D\uDD20 **Letters Available:**\n`"
                + new String(lettersAvailable).toUpperCase() + "`")).block();
    }

    @Override
    protected String getInvalidInputMessage(String input) {
        if (input.length() != word.length()) {
            return "Your word must be **" + word.length() + "** letters long!";
        } else if (!isValidWord(input)) {
            return "Your word contains at least one unavailable letter!";
        }
        return super.getInvalidInputMessage(input);
    }

    @Override
    protected boolean isValidInput(String input) {
        return input.matches("[a-z]{" + word.length() + "}") && isValidWord(input);
    }

    private boolean isValidWord(String guess) {
        for (int i = 0; i < guess.length(); i++) {
            if (!letterIsAvailable(guess.charAt(i)) || (foundLetters[i] && guess.charAt(i) != word.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private boolean letterIsAvailable(char c) {
        for (char availableLetter : lettersAvailable) {
            if (c == availableLetter) {
                return true;
            }
        }
        return false;
    }


    private void addToBoard() {
        guessesDisplay += GameEmoji.wordToLetterEmojis(lastGuess) + "\n";

        char[] lettersUnguessed = word.toCharArray();

        for (int i = 0; i < foundLetters.length; i++) {
            if (foundLetters[i]) {
                lettersUnguessed[i] = ' ';
            }
        }

        for (int i = 0; i < lastGuess.length(); i++) {
            if (foundLetters[i]) {
                guessesDisplay += GameEmoji.CHECKMARK;
            } else { //TODO refactor this logic
                boolean found = false;
                for (int j = 0; j < lettersUnguessed.length; j++) {
                    if (lettersUnguessed[j] == lastGuess.charAt(i)) {
                        lettersUnguessed[j] = ' ';
                        found = true;
                        break;
                    }
                }
                if (found) {
                    guessesDisplay += GameEmoji.DOUBLE_ARROW;
                } else {
                    guessesDisplay += GameEmoji.X;
                }
            }
            guessesDisplay += " ";
        }
        guessesDisplay += "\n";
    }

    @Override
    protected String getBoard() {
        return guessesDisplay;
    }

}
