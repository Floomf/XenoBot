package discord.core.game;

import discord.manager.UserManager;
import discord.util.DiscordColor;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import org.slf4j.LoggerFactory;

public abstract class MultiplayerGame extends BaseGame {

    private final Member[] players;
    private Member playerThisTurn;

    public MultiplayerGame(String gameTitle, TextChannel channel, Member[] players, int betAmount) {
        super(gameTitle, channel, betAmount);
        this.players = players;
        this.playerThisTurn = players[0];
        LoggerFactory.getLogger(getClass()).info("Game created with players: "
                + players[0].getTag() + " , " + players[1].getTag());
    }

    abstract protected String getForfeitMessage(Member forfeiter);

    abstract protected String getIdleMessage(Member idler);

    protected String getInvalidInputMessage() {
        return "**Invalid input.**\n(You can type **ff** to forfeit)";
    }

    public final boolean playerIsInGame(Member player) {
        for (Member p : players) {
            if (p.equals(player)) {
                return true;
            }
        }
        return false;
    }

    protected final synchronized void onPlayerInput(String input, Member player) {
        if (!super.isActive()) return;

        if (input.equals("forfeit") || input.equals("ff")) {
            win(getForfeitMessage(player), getOtherPlayer(player));
            return;
        }

        if (player.equals(playerThisTurn)) {
            if (isValidInput(input)) {
                onTurn(input);
                if (super.isActive()) { //kinda messy
                    setupNextTurn();
                }
            } else {
                Message invalidMessage = getChannel().createMessage(getInvalidInputMessage()).block();
                try {
                    Thread.sleep(1250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                invalidMessage.delete().block();
            }
        }
    }

    public void onPlayerReaction(ReactionEmoji emoji, Member player) {
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
        end();
        if (getBetAmount() > 0 && UserManager.databaseContainsUser(winner) ) {
            UserManager.getDUserFromMember(winner).addBalance(getBetAmount());
            UserManager.getDUserFromUser(getOtherPlayer(winner)).addBalance(-super.getBetAmount());
            winMessage += "\n\n💵 **$" + getBetAmount() + " won.**";
        }
        //some games don't want getBoard() to display at end, so don't use setInfoDisplay()
        setGameDisplay(winMessage, DiscordColor.GREEN);
    }

    protected void setupNextTurn() {
        playerThisTurn = getPNextTurn();
        super.setupNextTurn();
    }

    protected final Member getPNextTurn() {
        return players[super.getTurn() % players.length];
    }

    protected final Member getPThisTurn() {
        return playerThisTurn;
    }

    protected final Member getOtherPlayer(Member player) {
        return player.equals(playerThisTurn) ? getPNextTurn() : playerThisTurn;
    }

}
