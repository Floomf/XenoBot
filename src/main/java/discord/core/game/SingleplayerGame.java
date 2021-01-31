package discord.core.game;

import discord.manager.UserManager;
import discord.util.DiscordColor;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import org.slf4j.LoggerFactory;

public abstract class SingleplayerGame extends BaseGame {

    private final Member player;

    public SingleplayerGame(String gameTitle, TextChannel channel, Member player, int betAmount) {
        super(gameTitle, channel, betAmount);
        this.player = player;
        LoggerFactory.getLogger(getClass()).info("Game created with player: " + player.getTag());
    }

    abstract protected String getForfeitMessage();

    abstract protected String getIdleMessage();

    @Override
    protected void onEnd() {};

    public final boolean playerIsInGame(Member player) {
        return this.player.equals(player);
    }

    protected synchronized void onPlayerInput(String input, Member player) {
        if (!super.isActive()) return; //this is the way i thought of

        if (input.equalsIgnoreCase("forfeit") || input.equalsIgnoreCase("ff")) {
            //message.delete().doOnError(e -> super.setGameDisplay("I don't have permission to delete messages! Ended game.")).onErrorStop().block();
            lose(getForfeitMessage());
            return;
        }

        if (isValidInput(input)) {
            onTurn(input);
            if (super.isActive()) { //kinda messy
                setupNextTurn();
            }
        } else {
            Message invalidMessage = getChannel().createMessage("**Invalid input.**\n(You can type **ff** to forfeit)").block();
            try {
                Thread.sleep(1250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            invalidMessage.delete().block();
        }
    }

    public final void onPlayerReaction(ReactionEmoji emoji, Member player) {
        String unicode = emoji.asUnicodeEmoji().map(ReactionEmoji.Unicode::getRaw).orElse("");
        if (unicode.equals(GameEmoji.EXIT)) {
            lose(getForfeitMessage());
        } else if (isValidInput(unicode)) {
            onTurn(unicode);
            if (super.isActive()) {
                super.setupNextTurn();
            }
        }
        //super.getGameMessage().removeReaction(emoji, player.getId()).block();
    }

    protected void onTimeout() {
        lose(getIdleMessage());
    }

    protected void win(String winMessage, int winAmount) {
        end();
        if (winAmount > 0 && UserManager.databaseContainsUser(player) ) {
            String verb = super.getBetAmount() > 0 ? "won" : "earned";
            winMessage += "\n\n💵 **$" + winAmount + " " + verb + ".**";
            UserManager.getDUserFromMember(player).addBalance(winAmount);
        }

        //some games don't want getBoard() to display at end, so don't use setInfoDisplay()
        setGameDisplay(winMessage, DiscordColor.GREEN);
    }

    protected final void win(String winMessage) {
        win(winMessage + "\n\n" + getBoard(), getBetAmount());
    }

    //used for single player games
    protected final void lose(String loseMessage) {
        end();
        if (super.getBetAmount() > 0) {
            UserManager.getDUserFromMember(player).addBalance(-super.getBetAmount());
            loseMessage += "\n\n💵 **$" + super.getBetAmount() + " lost.**";
        }
        setGameDisplay(loseMessage, DiscordColor.RED);
    }

    protected final Member getPlayer() {
        return player;
    }

}
