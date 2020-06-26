package discord.command.game.blackjack;

import java.util.ArrayList;

public class Hand {

    private final ArrayList<Card> cards;
    private boolean hidden;

    public Hand(boolean hidden) {
        this.cards = new ArrayList<>();
        this.hidden = hidden;
    }

    public void addCard(Card card) {
        cards.add(card);
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public boolean hasBusted() {
        return getTotal() > 21;
    }

    public boolean isBlackjack() {
        return cards.size() == 2 && getTotal() == 21;
    }

    public int getTotal() {
        int i = 0;
        for (Card card : cards) {
            i += card.getNumValue();
        }
        for (Card card : cards) { //2 loops, can it be minimized to one?
            if (i > 21 && card.isType(Card.TYPE.ACE)) {
                i -= 10;
            }
        }
        return i;
    }

    @Override
    public String toString() {
        String s = "";
        if (hidden) {
            s += cards.get(0).toString() + " ??```";
        } else {
            for (Card card : cards) {
                s += card.toString() + " ";
            }
            s += "```Total: **" + getTotal() + "**";
        }
        return s;
    }
}
