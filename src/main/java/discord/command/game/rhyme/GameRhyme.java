package discord.command.game.rhyme;

import discord.core.game.MultiplayerGame;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.TextChannel;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class GameRhyme extends MultiplayerGame {

    private String wordToRhyme;
    private List<String> unguessedRhymes;
    private List<String> guessedRhymes;

    private final Thread turnTimerThread = new Thread(this::startTurnTimer);
    private final HashMap<Member, Integer> scoresMap = new HashMap<>();

    public GameRhyme(String gameTitle, TextChannel channel, Member[] players, int betAmount) {
        super(gameTitle, channel, players, betAmount);
    }

    @Override
    protected String getForfeitMessage(Member forfeiter) {
        return forfeiter.getMention() + " forfeited.\n" + super.getOtherPlayer(forfeiter).getMention() + " wins!";
    }

    @Override
    protected String getIdleMessage(Member idler) {
        return idler.getMention() + " failed to go in time.\n" + super.getOtherPlayer(idler).getMention() + " wins!";
    }

    @Override
    protected String getInvalidInputMessage() {
        return "**That word doesn't rhyme or was already rhymed!**";
    }

    @Override
    protected void setup() {
        unguessedRhymes = new ArrayList<>();
        guessedRhymes = new ArrayList<>();
        scoresMap.put(super.getPThisTurn(), 0);
        scoresMap.put(super.getPNextTurn(), 0);
        generateNewRhyme();
    }

    private void generateNewRhyme() {
        Random r = new Random();
        String firstLetters = "abcdefghijklmnoprstuw";
        String lettersToAdd = "?????";
        String fetchURL = "https://api.datamuse.com/words?max=10&sp=";
        fetchURL += firstLetters.charAt(r.nextInt(firstLetters.length())) + "?";
        fetchURL += lettersToAdd.substring(0, r.nextInt(lettersToAdd.length()) + 1);

        JSONArray wordsJson = Unirest.get(fetchURL).asJson().getBody().getArray();
        JSONObject wordJson = wordsJson.getJSONObject(r.nextInt(wordsJson.length()));

        JSONArray rhymesJson = Unirest.get("https://api.datamuse.com/words?max=300&rel_rhy=" + wordJson.getString("word"))
                .asJson().getBody().getArray();

        if (rhymesJson.length() < 45) {
            generateNewRhyme();
            return;
        }

        wordToRhyme = wordJson.getString("word");

        for (int i = 0; i < rhymesJson.length(); i++) {
            unguessedRhymes.add(rhymesJson.getJSONObject(i).getString("word"));
        }
    }

    @Override
    protected String getFirstDisplay() {
        return "**Go!** You start, " + getPThisTurn().getMention() + "\n" + getBoard();
    }

    @Override
    protected void onStart() {
        super.registerMessageListener();
        turnTimerThread.start();
    }

    @Override
    protected void onEnd() {
        if (Thread.currentThread().getId() != turnTimerThread.getId()) {
            turnTimerThread.interrupt();
        }
    }

    private void startTurnTimer() {
        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(20));
            onWordFail();
            if (super.isActive()) {
                startTurnTimer();
            }
        } catch (InterruptedException e) {
            if (!super.isActive()) {
                Thread.currentThread().interrupt();
            } else {
                startTurnTimer();
            }
        }
    }

    private synchronized void onWordFail() {
        scoresMap.put(GameRhyme.super.getPNextTurn(), scoresMap.get(GameRhyme.super.getPNextTurn()) + 1);

        if (scoresMap.values().stream().anyMatch(i -> i == 2)) {
            findWinner();
        } else {
            setupNextTurn();
            unguessedRhymes.clear();
            guessedRhymes.clear();
            generateNewRhyme();
            setInfoDisplay(super.getPThisTurn(), super.getPNextTurn().getMention()
                    + " **failed to rhyme in time!** New round.\nYou start, " + super.getPThisTurn().getMention());
        }
    }

    private void findWinner() {
        if (scoresMap.get(getPThisTurn()) > scoresMap.get(getPNextTurn())) {
            win("🏆 " + getPThisTurn().getMention() + " wins!", getPThisTurn());
        } else {
            win("🏆 " + getPNextTurn().getMention() + " wins!", getPNextTurn());
        }
    }

    @Override
    protected void onTurn(String input) {
        unguessedRhymes.remove(input);
        guessedRhymes.add(input);

        if (unguessedRhymes.isEmpty()) { //this will "never" happen but just in case
            super.win(super.getPThisTurn().getMention()+ " found the last rhyme! **INSTANT WIN!**", super.getPThisTurn());
        } else {
            super.setInfoDisplay(super.getPNextTurn(),
                    super.getPThisTurn().getMention() + " rhymed with **" + input + "**!\n" +
                            "Go " + super.getPNextTurn().getMention() + "!");
            turnTimerThread.interrupt();
        }
    }

    @Override
    protected boolean isValidInput(String input) {
        return unguessedRhymes.contains(input);
    }

    @Override
    protected String getBoard() { //guessedRhymes can technically exceed the message limit, but probably never will
        return "**Enter what rhymes with:**\n```fix\n" + wordToRhyme.toUpperCase()
                + "```\n\n**Already Rhymed:**\n" + guessedRhymes.toString();
    }
}
