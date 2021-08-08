package discord.command.game.blackjack;

import discord.core.game.SingleplayerGame;
import discord.manager.UserManager;
import discord.util.BotUtils;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.component.LayoutComponent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.TextChannel;

import java.util.ArrayList;
import java.util.Random;

public class GameBlackjack extends SingleplayerGame {

    private static final ArrayList<Card> DECK_CONSTANT = new ArrayList<>();
    private static final String[] SEX = {"👨", "👩"};
    private static final String[] SKIN_MODIFIERS = {"🏻","🏼","🏽","🏾","🏿"};

    static {
        for (Card.SUIT suit : Card.SUIT.values()) {
            for (int i = 2; i <= 10; i++) {
                DECK_CONSTANT.add(new Card(suit, Card.TYPE.NUMBER, i));
            }
            DECK_CONSTANT.add(new Card(suit, Card.TYPE.JACK, 10));
            DECK_CONSTANT.add(new Card(suit, Card.TYPE.QUEEN, 10));
            DECK_CONSTANT.add(new Card(suit, Card.TYPE.KING, 10));
            DECK_CONSTANT.add(new Card(suit, Card.TYPE.ACE, 11));
        }
    }

    private final ArrayList<Card> gameDeck = new ArrayList<>(DECK_CONSTANT);
    private String dealerEmoji;

    private final Hand playerHand = new Hand(false);
    private final Hand dealerHand = new Hand(true);

    public GameBlackjack(String gameTitle, TextChannel channel, Member player, int betAmount) {
        super(gameTitle, channel, player, betAmount);
    }

    private static String getRandomDealer() {
        Random rand = new Random();
        return SEX[rand.nextInt(SEX.length)] + SKIN_MODIFIERS[rand.nextInt(SKIN_MODIFIERS.length)];
    }

    @Override
    protected String getForfeitMessage() {
        return "";
    }

    @Override
    protected String getIdleMessage() {
        return "You failed to go in time.";
    }

    @Override
    protected void setup() {
        dealerEmoji = getRandomDealer();
        playerHand.addCard(drawCard());
        dealerHand.addCard(drawCard());
        playerHand.addCard(drawCard());
        dealerHand.addCard(drawCard());
    }

    @Override
    protected LayoutComponent[] getComponents() {
        return new LayoutComponent[] {
                ActionRow.of(Button.success("hit", "Hit"),
                        Button.primary("stay", "Stay"),
                        Button.danger("double_down", "Double down"))
        };
    }

    @Override
    protected String getFirstDisplay() {
        return "**Hand dealt!** What's your move?\n\n" + getBoard();
    }

    @Override
    protected void onStart() {
        //check for blackjack
        if (playerHand.isBlackjack()) {
            if (dealerHand.isBlackjack()) {
                dealerHand.setHidden(false);
                super.tie("**WOW!** Double blackjack! **Push.**\n\n" + getBoard());
            } else {
                super.win("**Blackjack!** " + BotUtils.getRandomGuildEmoji(getGameMessage().getGuild().block(),
                        new String[]{"Pog", "PogU"}) + "\n\n" + getBoard(), super.getBetAmount() * 3 / 2);
            }
            return;
        } else if (dealerHand.isBlackjack()) {
            dealerHand.setHidden(false);
            lose("**Dealer blackjack!**\n\n" + getBoard());
            return;
        }
        super.registerComponentListener();
    }

    @Override
    protected void onTurn(String input) {
        if (input.equals("hit")) {
            Card card = drawCard();
            playerHand.addCard(card);
            if (playerHand.hasBusted()) {
                super.lose("Drew a **" + card.toString() + "**! You busted.\n\n" + getBoard());
            } else {
                super.setInfoDisplay("Drew a **" + card.toString() + "**! Hit or stand?");
            }
        } else if (input.equals("stay")) {
            dealerHand.setHidden(false);
            super.setInfoDisplay("You stood.");
            commitStand();
        } else if (input.equals("double_down")) {
            super.setBetAmount(super.getBetAmount() * 2);
            Card card = drawCard();
            playerHand.addCard(card);
            if (playerHand.hasBusted()) {
                lose("**Double down!**\nDrew a **" + card.toString() + "**! You busted.\n\n" + getBoard());
            } else {
                dealerHand.setHidden(false);
                super.setInfoDisplay("**Double down!**\nDrew a **" + card.toString() + "**!");
                commitStand();
            }
        }
    }

    private void commitStand() {
        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        while (dealerHand.getTotal() < 17) {
            Card card = drawCard();
            dealerHand.addCard(card);
            super.setInfoDisplay("Dealer drew a **" + card.toString() + "**!");

            try {
                Thread.sleep(2500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (dealerHand.hasBusted()) {
            super.win("**Dealer busted!**");
        } else if (playerHand.getTotal() > dealerHand.getTotal()) {
            super.win("**You win!**");
        } else if (playerHand.getTotal() == dealerHand.getTotal()) {
            super.tie("**Push.**\n\n" + getBoard());
        } else {
            super.lose("**Dealer wins.**\n\n" + getBoard());
        }
    }

    @Override
    protected boolean isValidInput(String input) {
        if (input.equals("double_down")) {
            return gameDeck.size() == 48 && UserManager.getDUserFromMember(getPlayer()).getBalance() >= getBetAmount() * 2;
        }
        return true;
    }

    private Card drawCard() {
        Card card = gameDeck.get((int) (Math.random() * gameDeck.size()));
        gameDeck.remove(card);
        return card;
    }

    @Override
    protected String getBoard() {
        return "__Dealer's Hand__ " + dealerEmoji + "\n```yaml\n"+
                dealerHand.toString() +
                "\n\n__Your Hand__ 🃏\n```yaml\n" +
                playerHand.toString();
    }
}
