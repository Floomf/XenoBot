package discord.command.game.memory;

import discord.core.game.GameEmoji;
import discord.core.game.MultiplayerGame;
import discord.listener.EventsHandler;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.GuildEmoji;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

//TODO sleep on new thread?

public class GameMemory extends MultiplayerGame {

    class Square {

        final static String HIDDEN_EMOJI = ":white_medium_square:";
        final static String CLEARED_EMOJI = ":heavy_multiplication_x:";

        final String matchValue;
        String emoji;

        Square(String matchValue) {
            this.matchValue = matchValue;
            this.emoji = HIDDEN_EMOJI;
        }

        void reveal() {
            emoji = matchValue;
        }

        void hide() {
            emoji = HIDDEN_EMOJI;
        }

        void clear() {
            emoji = CLEARED_EMOJI;
        }

        boolean isHidden() {
            return emoji.equals(HIDDEN_EMOJI);
        }

        boolean isCleared() {
            return emoji.equals(CLEARED_EMOJI);
        }

        public boolean matches(Square other) {
            return matchValue.equals(other.matchValue);
        }

        @Override
        public String toString() {
            return emoji;
        }

    }

    private final Square[][] board = new Square[4][4];

    private Message boardMessage;
    private Square previousSquareSelected;

    private final HashMap<Member, Integer> scoresMap = new HashMap<>();

    public GameMemory(String gameTitle, TextChannel channel, Member[] players, int betAmount) {
        super(gameTitle, channel, players, betAmount);
    }

    @Override
    protected boolean useEmbed() {
        return false;
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
    protected void setup() {
        scoresMap.put(super.getPThisTurn(), 0);
        scoresMap.put(super.getPNextTurn(), 0);

        Random rand = new Random();
        List<String> emojis = getChannel().getClient().getGuildById(EventsHandler.THE_REALM_ID)
                .flatMapMany(Guild::getEmojis).map(GuildEmoji::asFormat).collectList().block();

        //fill in board with 8 pairs of squares
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j+=2) {
                String emoji = emojis.get(rand.nextInt(emojis.size()));
                emojis.remove(emoji);
                board[i][j] = new Square(emoji);
                board[i][j + 1] = new Square(emoji);
            }
        }

        //shuffle them
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                int x = rand.nextInt(board.length);
                int y = rand.nextInt(board[i].length);

                Square temp = board[i][j];
                board[i][j] = board[x][y];
                board[x][y] = temp;
            }
        }

        boardMessage = getChannel().createMessage(getActualBoard()).block();
    }

    @Override
    protected String getFirstDisplay() {
        return "You start off, " + super.getPThisTurn().getMention() + "!\n\nEnter the coord of the square to flip over, like **b2**";
    }

    @Override
    protected void onStart() {
        registerMessageListener(2);
    }

    @Override
    protected void onTurn(String input) {
        Square pick = getSquareFromCoords(input);
        pick.reveal();
        updateActualBoard();

        if (previousSquareSelected != null) {//TODO using global field, avoid?
            try {
                Thread.sleep(2500);
            } catch (InterruptedException e) {
                //TODO avoid
            }
            if (pick.matches(previousSquareSelected)) {
                scoresMap.put(super.getPThisTurn(), scoresMap.get(getPThisTurn()) + 1);

                pick.clear();
                previousSquareSelected.clear();
                previousSquareSelected = null;
                updateActualBoard();

                if (scoresMap.values().stream().anyMatch(score -> score == (board.length + board[0].length) / 2 + 1)) {
                    revealAllSquares();
                    updateActualBoard();
                    Member winner = scoresMap.keySet().stream().filter(m -> scoresMap.get(m) == 5).findFirst().get();
                    win(winner.getMention() + " found five matches! **Winner!**", winner);
                } else if (scoresMap.get(getPThisTurn()) == 4 && scoresMap.get(getPNextTurn()) == 4) {
                    revealAllSquares();
                    updateActualBoard();
                    tie("Even matches! Tie!");
                } else {
                    super.setInfoDisplay(":white_check_mark: **It's a match!**\nYour turn again, " + super.getPThisTurn().getMention());
                    super.setupNextTurn();
                }
            } else {
                previousSquareSelected.hide();
                previousSquareSelected = null;
                pick.hide();

                super.setInfoDisplay(":x: **No match here!**\nYour turn now, " + super.getPNextTurn().getMention());
                updateActualBoard();
            }
        } else {
            previousSquareSelected = pick;
            super.setInfoDisplay(super.getPThisTurn(), " flipped over " + pick.toString() + "! Flip another square.");
            setupNextTurn();
        }
    }

    private void updateActualBoard() {
        boardMessage.edit(spec -> spec.setContent(getActualBoard())).block();
    }

    private String getActualBoard() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < board.length; i++) {
            sb.append(GameEmoji.intToNumberEmoji(board.length - i));
            for (Square card : board[i]) {
                sb.append(" ").append(card);
            }
            sb.append("\n");
        }
        sb.append("⏺ 🇦 🇧 🇨 🇩");
        return sb.toString();
    }

    private void revealAllSquares() {
        for (Square[] row : board) {
            for (Square square :  row) {
                square.reveal();
            }
        }
    }

    private Square getSquareFromCoords(String coords) {
        return board[board.length - (coords.charAt(1) - '1') - 1][coords.charAt(0) - 'a'];
    }

    @Override
    protected boolean isValidInput(String input) {
        return input.matches("[a-d][1-4]") && getSquareFromCoords(input).isHidden();
    }

    //TODO redesign to have this as the actual board
    @Override
    protected String getBoard() {
        return "";
    }
}
