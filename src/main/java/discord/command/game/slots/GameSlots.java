package discord.command.game.slots;

import discord.core.game.SingleplayerGame;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.TextChannel;

import java.util.Random;

public class GameSlots extends SingleplayerGame {

    private final static String[] SYMBOLS = {"🍒", "🍇", "🔔", "💎", ":seven:"};
    private final static String[] MACHINE_COLORS = {":red_square:", ":orange_square:", ":yellow_square:", ":green_square:",
            ":blue_square:", ":purple_square:", ":brown_square:"};

    private String[] reelResult;
    private String[] reelDisplay;
    private String machineColor;

    public GameSlots(String gameTitle, TextChannel channel, Member player, int betAmount) {
        super(gameTitle, channel, player, betAmount);
    }

    @Override
    protected boolean useEmbed() {
        return false;
    }

    @Override
    protected String getForfeitMessage() {
        return "";
    }

    @Override
    protected String getIdleMessage() {
        return "You failed to spin in time.";
    }

    @Override
    protected void setup() {
        reelResult = getRandomReel();
        reelDisplay = new String[]{"❌", "❌", "❌"};
        machineColor = MACHINE_COLORS[(int) (Math.random() * MACHINE_COLORS.length)];
    }

    @Override
    protected String getFirstDisplay() {
        return getBoard();
    }

    @Override
    protected void onStart() {
        new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            onTurn("");
        }).start();
    }

    //It's all predetermined before the game even starts
    private static String[] getRandomReel() {
        Random rand = new Random();
        if (rand.nextInt(100) < 33) {
            int odds = rand.nextInt(100);
            if (odds < 1) {
                return new String[]{SYMBOLS[4], SYMBOLS[4], SYMBOLS[4]};
            } else if (odds < 10) {
                return new String[]{SYMBOLS[3], SYMBOLS[3], SYMBOLS[3]};
            } else if (odds < 20) {
                return new String[]{SYMBOLS[2], SYMBOLS[2], SYMBOLS[2]};
            } else if (odds < 38) {
                return new String[]{SYMBOLS[1], SYMBOLS[1], SYMBOLS[1]};
            } else {
                return new String[]{SYMBOLS[0], SYMBOLS[0], SYMBOLS[0]};
            }
        } else { //fake losing reel
            String first = SYMBOLS[rand.nextInt(SYMBOLS.length)];
            String second;
            String third = "";
            if (rand.nextInt(3) == 0) {
                second = first; //give them hope
            } else {
                second = SYMBOLS[rand.nextInt(SYMBOLS.length)];
            }

            do {
                third = SYMBOLS[rand.nextInt(SYMBOLS.length)];
            } while (third.equals(first));

            return new String[]{first, second, third};
        }
    }

    private static int getMultiplierForSymbol(String symbol) {
        if (symbol.equals(SYMBOLS[0])) {
            return 1;
        } else if (symbol.equals(SYMBOLS[1])) {
            return 2;
        } else if (symbol.equals(SYMBOLS[2])) {
            return 3;
        } else if (symbol.equals(SYMBOLS[3])) {
            return 5;
        } else if (symbol.equals(SYMBOLS[4])) {
            return 10;
        }
        return 0;
    }

    @Override
    protected void onTurn(String input) {
        reelDisplay[super.getTurn() - 1] = reelResult[super.getTurn() - 1];
        super.setInfoDisplay("");

        try {
            Thread.sleep(1250);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (super.getTurn() == 3) {
            if (reelResult[0].equals(reelResult[1]) && reelResult[0].equals(reelResult[2])) {
                super.win("**You win!**\n\n" + getBoard(), super.getBetAmount() * getMultiplierForSymbol(reelResult[0]));
            } else {
                super.lose("You lose.\n\n" + getBoard());
            }
        } else {
            setupNextTurn();
            onTurn(input);
        }
    }

    @Override
    protected boolean isValidInput(String input) {
        return true;
    }

    @Override
    protected String getBoard() {
        return machineColor + machineColor + machineColor + machineColor + machineColor + ":red_circle:\n"
                + machineColor + reelDisplay[0] + reelDisplay[1] + reelDisplay[2] + machineColor + ":spoon:\n"
                + machineColor + machineColor + machineColor + machineColor + machineColor + ":link:\n"
                + machineColor + machineColor + machineColor + machineColor + machineColor;
    }
}
