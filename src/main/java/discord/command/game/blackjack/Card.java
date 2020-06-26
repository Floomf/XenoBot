package discord.command.game.blackjack;

public class Card {

    enum SUIT {
        DIAMONDS, CLUBS, HEARTS, SPADES;

        @Override
        public String toString() {
            switch (this) {
                case DIAMONDS:
                    return "♦️";
                case CLUBS:
                    return "♣️";
                case HEARTS:
                    return "♥️";
                case SPADES:
                    return "♠️";
            }
            return "";
        }
    }

    enum TYPE {
        NUMBER, JACK, QUEEN, KING, ACE
    }

    private final SUIT suit;
    private final TYPE type;
    private final int numValue;

    public Card(SUIT suit, TYPE type, int numValue) {
        this.suit = suit;
        this.type = type;
        this.numValue = numValue;
    }

    public int getNumValue() {
        return numValue;
    }

    public boolean isType(TYPE type) {
        return this.type == type;
    }

    @Override
    public String toString() {
        if (type == TYPE.NUMBER) {
            return numValue + suit.toString();
        }
        return type.name().substring(0, 1) + suit.toString();
    }

}
