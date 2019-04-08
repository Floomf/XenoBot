package discord.command.game.twentythree;

import discord.core.game.ButtonGame;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class GameTwentyThree extends ButtonGame {        
    
    private static final int MAX_COUNT = 23;
    
    private int amount;
       
    public GameTwentyThree(IMessage message, IUser[] players) {       
        super(message, players);
        super.getButtonManager().addNumButtons(message, 3);
        amount = MAX_COUNT;
    }    
    
    @Override
    protected void onStart() {
        super.updateMessageDisplay("Game started! There are `" + amount + "` potatoes to snack on."
                + "\nYou may eat up to `3` per turn. The person to eat the last potato loses!"
                + "\n\nYou start off, " + super.getThisTurnUser());
    }   
     
    @Override
    protected void onTurn(int input) {
        amount -= input;
        String suffix = ((input > 1) ? "es" : "");
        if (amount > 0) {          
            super.updateMessageDisplay(super.getThisTurnUser().getName() + " ate `" + input + 
                    "` potato" + suffix + ". Your move, " + super.getNextTurnUser());
        } else {
            super.win(super.getNextTurnUser(), super.getThisTurnUser().getName()
                    + " ate the last potato" + suffix + ". **" + super.getNextTurnUser().getName() + "** wins!");
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

