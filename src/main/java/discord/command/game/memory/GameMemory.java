package discord.command.game.memory;

import discord.core.game.GameEmoji;
import discord.core.game.MultiplayerGame;
import discord.listener.EventsHandler;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.component.LayoutComponent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.reaction.ReactionEmoji;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

//TODO sleep on new thread?

public class GameMemory extends MultiplayerGame {

    class Square {

        final ReactionEmoji HIDDEN_EMOJI = ReactionEmoji.unicode("⬜");
        final ReactionEmoji CLEARED_EMOJI = ReactionEmoji.unicode("✖");

        final String value;
        final int id;

        final ReactionEmoji valueEmoji;
        ReactionEmoji currentEmoji;

        Square(String value, int id, ReactionEmoji emoji) {
            this.value = value;
            this.id = id;
            this.valueEmoji = emoji;
            this.currentEmoji = HIDDEN_EMOJI;
        }

        void reveal() {
            currentEmoji = valueEmoji;
        }

        void hide() {
            currentEmoji = HIDDEN_EMOJI;
        }

        void clear() {
            currentEmoji = CLEARED_EMOJI;
        }

        boolean isHidden() {
            return currentEmoji.equals(HIDDEN_EMOJI);
        }

        boolean isCleared() {
            return currentEmoji.equals(CLEARED_EMOJI);
        }

        public boolean matches(Square other) {
            return value.equals(other.value);
        }
        public Button toButton() {
            if (currentEmoji.equals(HIDDEN_EMOJI)) {
                return Button.secondary(value + id, currentEmoji);
            } if (currentEmoji.equals(CLEARED_EMOJI)) {
                return Button.secondary(value + id, currentEmoji).disabled();
            } else {
                return Button.primary(value + id, currentEmoji);
            }
        }

    }

    private final HashMap<String, Square> buttonMap = new HashMap<>();
    private final Square[] squares = new Square[16];

    private final Square[][] board = new Square[4][4];

    //private Message boardMessage;
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
    protected LayoutComponent[] getComponents() {
        ArrayList<LayoutComponent> components = new ArrayList<>();

        for (int i = 0; i < squares.length; i += 4) {
            components.add(ActionRow.of(squares[i].toButton(), squares[i + 1].toButton(),
                    squares[i + 2].toButton(), squares[i + 3].toButton()));
        }

        return components.toArray(new LayoutComponent[0]);
    }

    @Override
    protected void setup() {
        scoresMap.put(super.getPThisTurn(), 0);
        scoresMap.put(super.getPNextTurn(), 0);

        Random rand = new Random();
        List<ReactionEmoji> emojis = getChannel().getClient().getGuildById(EventsHandler.THE_REALM_ID)
                .flatMapMany(Guild::getEmojis).map(emoji -> ReactionEmoji.of(emoji.getData())).collectList().block();

        for (int i = 0; i < squares.length; i+=2) {
            ReactionEmoji emoji = emojis.get(rand.nextInt(emojis.size()));
            emojis.remove(emoji);

            String value = emoji.asCustomEmoji().get().getName();
            squares[i] = new Square(value, 1, emoji);
            squares[i + 1] = new Square(value, 2, emoji);
        }

        for (int i = 0; i < squares.length; i++) {
            int index = rand.nextInt(board.length);
            Square temp = squares[index];
            squares[index] = squares[i];
            squares[i] = temp;
        }

        for (Square square : squares) {
            buttonMap.put(square.value + square.id, square);
        }

        //fill in board with 8 pairs of squares
        /*for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j+=2) {
                String emoji = emojis.get(rand.nextInt(emojis.size()));
                emojis.remove(emoji);
                board[i][j] = new Square(emoji, 1);
                board[i][j + 1] = new Square(emoji, 2);
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
        }*/

        //boardMessage = getChannel().createMessage(getActualBoard()).block();
    }

    @Override
    protected String getFirstDisplay() {
        return "You start off, " + super.getPThisTurn().getMention() + "!\n\nClick on a square to flip it over.";
    }

    @Override
    protected void onStart() {
        //registerMessageListener(2);
        registerComponentListener();
    }

    private Square getFromInput(String input) {
        for (Square square : squares) {
            if (input.equals(square.value + square.id)) {
                return square;
            }
        }
        return null;
    }

    @Override
    protected void onTurn(String input) {
        Square pick = getFromInput(input);
        //Square pick = getSquareFromCoords(input);
        String emoji = "<:" + pick.valueEmoji.asCustomEmoji().get().getName() + ":" + pick.valueEmoji.asCustomEmoji().get().getId().asString() + ">";


        pick.reveal();
        updateActualBoard();

        if (previousSquareSelected != null) {//TODO using global field, avoid?
            super.setInfoDisplay(super.getPThisTurn(), " flipped over " + emoji + "!");
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
                return;
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                //TODO avoid
            }
            previousSquareSelected.hide();
            previousSquareSelected = null;
            pick.hide();

            super.setInfoDisplay(":x: **No match here!**\nYour turn now, " + super.getPNextTurn().getMention());
            updateActualBoard();
        } else {
            previousSquareSelected = pick;
            super.setInfoDisplay(super.getPThisTurn(), " flipped over " + emoji + "! Flip another square.");
            setupNextTurn();
        }
    }

    private void updateActualBoard() {
        //boardMessage.edit(spec -> spec.setContent(getActualBoard())).block();
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
        for (Square square : squares) {
            square.reveal();
        }
    }

    private Square getSquareFromCoords(String coords) {
        return board[board.length - (coords.charAt(1) - '1') - 1][coords.charAt(0) - 'a'];
    }

    @Override
    protected boolean isValidInput(String input) {
        return true;
        //return input.matches("[a-d][1-4]") && getSquareFromCoords(input).isHidden();
    }

    //TODO redesign to have this as the actual board
    @Override
    protected String getBoard() {
        return "";
    }
}
