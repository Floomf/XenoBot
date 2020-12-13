package discord.core.game;

import discord.manager.GameManager;
import discord.manager.UserManager;
import discord.util.DiscordColor;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.reaction.ReactionEmoji;

public abstract class SingleplayerGame extends BaseGame {

    private final Member player;

    public SingleplayerGame(String gameTitle, TextChannel channel, Member player, int betAmount) {
        super(gameTitle, channel, betAmount);
        this.player = player;
    }

    abstract protected String getForfeitMessage();

    abstract protected String getIdleMessage();

    @Override
    protected void onEnd() {};

    public final boolean playerIsInGame(Member player) {
        return this.player.equals(player);
    }

    protected void onPlayerMessage(Message message, Member player) {
        if (message.getContent().equalsIgnoreCase("forfeit") || message.getContent().equalsIgnoreCase("ff")) {
            message.delete().doOnError(e -> super.setGameDisplay("I don't have permission to delete messages! Ended game.")).onErrorStop().block();
            lose(getForfeitMessage());
            return;
        }

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
        //message.delete().doOnError(e -> super.setGameDisplay("I don't have permission to delete messages! Ended game.")).onErrorStop().block();
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
        if (winAmount > 0 && UserManager.databaseContainsUser(player) ) {
            if (super.getBetAmount() == 0) { //not a betting game
                long winnerID = player.getId().asLong();
                if (GameManager.usersMoneyEarned.containsKey(winnerID)
                        && GameManager.usersMoneyEarned.get(winnerID) >= GameManager.EARN_LIMIT) {
                    winMessage += "\n\n💵 **No money earned.**\n*(Limit resets at 9AM daily)*";
                } else {
                    if (!GameManager.usersMoneyEarned.containsKey(winnerID)) {
                        GameManager.usersMoneyEarned.put(winnerID, 0);
                    }

                    if (GameManager.usersMoneyEarned.get(winnerID) + winAmount >= GameManager.EARN_LIMIT) { //shave off extra money to match the limit
                        winAmount = GameManager.EARN_LIMIT - GameManager.usersMoneyEarned.get(winnerID);
                        winMessage += "\n\n💵 **$" + winAmount + " earned.**\n(Earning limit reached)";
                    } else {
                        winMessage += "\n\n💵 **$" + winAmount + " earned.**";
                    }
                    GameManager.usersMoneyEarned.put(winnerID, GameManager.usersMoneyEarned.get(winnerID) + winAmount);
                    UserManager.getDUserFromMember(player).addBalance(winAmount);
                }
            } else {
                UserManager.getDUserFromMember(player).addBalance(winAmount);
                winMessage += "\n\n💵 **$" + winAmount + " won.**";
            }
        }

        //some games don't want getBoard() to display at end, so don't use setInfoDisplay()
        setGameDisplay(winMessage, DiscordColor.GREEN);
        end();
    }

    protected final void win(String winMessage) {
        win(winMessage + "\n\n" + getBoard(), getBetAmount());
    }

    //used for single player games
    protected final void lose(String loseMessage) {
        if (super.getBetAmount() > 0) {
            UserManager.getDUserFromMember(player).addBalance(-super.getBetAmount());
            loseMessage += "\n\n💵 **$" + super.getBetAmount() + " lost.**";
        }
        setGameDisplay(loseMessage, DiscordColor.RED);
        end();
    }

    protected final Member getPlayer() {
        return player;
    }

}
