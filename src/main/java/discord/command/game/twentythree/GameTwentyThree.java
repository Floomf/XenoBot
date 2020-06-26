package discord.command.game.twentythree;

import discord4j.core.object.entity.Member;
import discord.core.game.ButtonGame;
import discord4j.core.object.entity.Message;

public class GameTwentyThree extends ButtonGame {

    private static final int MAX_COUNT = 23;

    private int amount;

    public GameTwentyThree(Message message, Member[] players, int betAmount) {
        super(message, players, betAmount);
        super.getButtonManager().addNumButtons(message, 3);
        amount = MAX_COUNT;
    }

    @Override
    protected String getGameTitle() {
        return "23";
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
    protected void onStart() {
        super.setInfoDisplay(super.getPThisTurn(),"Game started! There are **" + amount + "** cookies to snack on."
                + "\nYou may eat up to **3** per turn. The person to eat the last cookie loses!"
                + "\n\nYou start off, " + super.getPThisTurn().getMention());
    }

    @Override
    protected void onTurn(int input) {
        amount -= input;
        String suffix = ((input > 1) ? "s" : "");
        if (amount > 0) {
            super.setInfoDisplay(super.getPNextTurn(), super.getPThisTurn().getMention() + " ate **" + input +
                    "** cookie" + suffix + ". Your move, " + super.getPNextTurn().getMention());
        } else {
            super.win(super.getPThisTurn().getMention() + " ate the last cookie"
                    + suffix + ".\n" + super.getPNextTurn().getMention() + " wins!", super.getPNextTurn());
        }
    }

    @Override
    protected boolean isValidInput(int input) {
        return (input <= amount);
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
