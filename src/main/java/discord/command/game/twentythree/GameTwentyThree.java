package discord.command.game.twentythree;

import discord.core.game.GameEmoji;
import discord.core.game.MultiplayerGame;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.TextChannel;

public class GameTwentyThree extends MultiplayerGame {

    private static final int MAX_COUNT = 23;

    private int amount;

    public GameTwentyThree(String gameTitle, TextChannel channel, Member[] players, int betAmount) {
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
    protected void setup() {
        amount = MAX_COUNT;
    }

    @Override
    protected String getFirstDisplay() {
        return "Game started! There are **" + amount + "** cookies to snack on."
                + "\nYou may eat up to **3** per turn. The person to eat the last cookie loses!"
                + "\n\nYou start off, " + super.getPThisTurn().getMention();
    }

    @Override
    protected void onStart() {
        super.registerReactionListener();
        super.addEmojiReaction(GameEmoji.ONE);
        super.addEmojiReaction(GameEmoji.TWO);
        super.addEmojiReaction(GameEmoji.THREE);
    }

    @Override
    protected void onTurn(String input) {
        amount -= GameEmoji.numberEmojiToInt(input);
        String suffix = ((GameEmoji.numberEmojiToInt(input) > 1) ? "s" : "");
        if (amount > 0) {
            super.setInfoDisplay(super.getPNextTurn(), super.getPThisTurn().getMention() + " ate **" + input +
                    "** cookie" + suffix + ".\nYour move, " + super.getPNextTurn().getMention());
        } else {
            super.win(super.getPThisTurn().getMention() + " ate the last cookie"
                    + suffix + ".\n" + super.getPNextTurn().getMention() + " wins!", super.getPNextTurn());
        }
    }

    @Override
    protected boolean isValidInput(String input) {
        return (GameEmoji.numberEmojiToInt(input) <= amount);
    }

    @Override
    protected String getBoard() {
        StringBuilder sb = new StringBuilder();
        sb.append("**").append(amount).append("** - ");
        for (int i = 1; i <= amount; i++) {
            sb.append("🍪");
        }
        return sb.toString();
    }

}
