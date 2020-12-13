package discord.core.game;

import discord.manager.UserManager;
import discord.util.DiscordColor;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.reaction.ReactionEmoji;

public abstract class MultiplayerGame extends BaseGame {

    private final Member[] players;
    private Member playerThisTurn;

    public MultiplayerGame(String gameTitle, TextChannel channel, Member[] players, int betAmount) {
        super(gameTitle, channel, betAmount);
        this.players = players;
        this.playerThisTurn = players[0];
    }

    abstract protected String getForfeitMessage(Member forfeiter);

    abstract protected String getIdleMessage(Member idler);

    public final boolean playerIsInGame(Member player) {
        for (Member p : players) {
            if (p.equals(player)) {
                return true;
            }
        }
        return false;
    }

    protected final void onPlayerMessage(Message message, Member player) {
        if (message.getContent().equalsIgnoreCase("forfeit") || message.getContent().equalsIgnoreCase("ff")) {
            message.delete().doOnError(e -> super.setGameDisplay("I don't have permission to delete messages! Ended game.")).onErrorStop().block();
            win(getForfeitMessage(player), getOtherPlayer(player));
            return;
        }

        if (player.equals(getPThisTurn())) {
            String input = message.getContent().toLowerCase().trim();
            if (isValidInput(input)) {
                onTurn(input);
                if (super.isActive()) { //kinda messy
                    setupNextTurn();
                }
            } else {
                Message invalidMessage = message.getChannel().block().createMessage("**Invalid input.**\n(You can type **ff** to forfeit)").block();
                try {
                    Thread.sleep(1250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                invalidMessage.delete().block();
            }
        }
        message.delete().doOnError(e -> super.setGameDisplay("I don't have permission to delete messages! Ended game.")).onErrorStop().block();
    }

    public final void onPlayerReaction(ReactionEmoji emoji, Member player) {
        String raw = emoji.asUnicodeEmoji().map(ReactionEmoji.Unicode::getRaw).orElse("");
        if (raw.equals(GameEmoji.EXIT)) {
            win(getForfeitMessage(player), getOtherPlayer(player));
        } else if (player.equals(playerThisTurn) && isValidInput(raw)) {
            onTurn(emoji.asUnicodeEmoji().map(ReactionEmoji.Unicode::getRaw).orElse(""));
            if (super.isActive()) {
                setupNextTurn();
            }
        }
        //super.getGameMessage().removeReaction(emoji, player.getId()).block();
    }

    protected final void onTimeout() {
        win(getIdleMessage(playerThisTurn), getOtherPlayer(playerThisTurn));
    }

    protected void onEnd() {}; //bad design?

    protected final void win(String winMessage, Member winner) {
        if (getBetAmount() > 0 && UserManager.databaseContainsUser(winner) ) {
            UserManager.getDUserFromMember(winner).addBalance(getBetAmount());
            UserManager.getDUserFromUser(getOtherPlayer(winner)).addBalance(-super.getBetAmount());
            winMessage += "\n\n💵 **$" + getBetAmount() + " won.**";
        }
        //some games don't want getBoard() to display at end, so don't use setInfoDisplay()
        setGameDisplay(winMessage, DiscordColor.GREEN);
        end();
    }

    protected final void setupNextTurn() {
        playerThisTurn = getPNextTurn();
        super.setupNextTurn();
    }

    protected final Member getPNextTurn() {
        return players[super.getTurn() % players.length];
    }

    protected final Member getPThisTurn() {
        return playerThisTurn;
    }

    //Works with 1-2 player games
    protected final Member getOtherPlayer(Member player) {
        if (player.equals(playerThisTurn)) {
            return getPNextTurn();
        } else {
            return playerThisTurn;
        }
    }

}
