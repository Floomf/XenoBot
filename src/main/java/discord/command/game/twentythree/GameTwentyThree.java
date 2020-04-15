package discord.command.game.twentythree;

import discord4j.core.object.entity.Member;
import discord.core.game.ButtonGame;
import discord4j.core.object.entity.Message;

public class GameTwentyThree extends ButtonGame {

    private static final int MAX_COUNT = 23;

    private int amount;

    public GameTwentyThree(Message message, Member[] players) {
        super(message, players);
        super.getButtonManager().addNumButtons(message, 3);
        amount = MAX_COUNT;
    }

    @Override
    protected String getGameTitle() {
        return "23";
    }

    @Override
    protected String getForfeitMessage(Member forfeiter) {
        return forfeiter.getMention() + " forfeited. " + super.getOtherUser(forfeiter).getMention() + " wins!";
    }

    @Override
    protected String getIdleMessage(Member idler) {
        return idler.getMention() + " failed to go in time. " + super.getOtherUser(idler).getMention() + " wins!";
    }

    @Override
    protected void onStart() {
        super.setInfoDisplay("Game started! There are `" + amount + "` potatoes to snack on."
                + "\nYou may eat up to `3` per turn. The person to eat the last potato loses!"
                + "\n\nYou start off, " + super.getPlayerThisTurn().getMention());
    }

    @Override
    protected void onTurn(int input) {
        amount -= input;
        String suffix = ((input > 1) ? "es" : "");
        if (amount > 0) {
            super.setInfoDisplay(super.getPlayerThisTurn().getDisplayName() + " ate `" + input +
                    "` potato" + suffix + ". Your move, " + super.getPlayerNextTurn().getMention());
        } else {
            super.win(super.getPlayerThisTurn().getDisplayName() + " ate the last potato"
                    + suffix + ". **" + super.getPlayerNextTurn().getDisplayName() + "** wins!");
        }
    }

    @Override
    protected boolean isValidInput(int input) {
        return (input <= amount);
    }

    @Override
    protected String getBoard() {
        StringBuilder sb = new StringBuilder();
        sb.append("`").append(amount).append("` - ");
        for (int i = 1; i <= amount; i++) {
            sb.append(":potato:");
        }
        return sb.toString();
    }

}
