package discord.command.game.akinator;

import com.markozajc.akiwrapper.Akiwrapper;
import com.markozajc.akiwrapper.AkiwrapperBuilder;
import com.markozajc.akiwrapper.core.entities.Guess;
import com.markozajc.akiwrapper.core.entities.Server;
import com.markozajc.akiwrapper.core.exceptions.ServerNotFoundException;
import discord.core.game.GameEmoji;
import discord.core.game.SingleplayerGame;
import discord.util.BotUtils;
import discord.util.DiscordColor;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.reaction.ReactionEmoji;

import java.net.URL;
import java.util.*;

public class GameAkinator extends SingleplayerGame {

    private Akiwrapper aw;
    private final HashMap<String, Akiwrapper.Answer> answerMap = new HashMap<>();
    private final List<Long> declinedGuesses = new ArrayList<>();
    private Guess currentGuess;

    private final static Server.GuessType[] GUESS_TYPES = {Server.GuessType.CHARACTER, Server.GuessType.OBJECT, Server.GuessType.ANIMAL};
    private int guessTypeIndex = 0;

    public GameAkinator(String gameTitle, TextChannel channel, Member player, int betAmount) {
        super(gameTitle, channel, player, 0);
    }

    @Override
    protected String getForfeitMessage() {
        return "See you next time!";
    }

    @Override
    protected String getIdleMessage() {
        return "Where did you go? See you later.";
    }

    @Override
    protected boolean useEmbed() {
        return true;
    }

    @Override
    protected void setup() {
        answerMap.put(GameEmoji.Y, Akiwrapper.Answer.YES);
        answerMap.put(GameEmoji.N, Akiwrapper.Answer.NO);
        answerMap.put(GameEmoji.D, Akiwrapper.Answer.DONT_KNOW);
        answerMap.put(GameEmoji.P, Akiwrapper.Answer.PROBABLY);
        answerMap.put(GameEmoji.U, Akiwrapper.Answer.PROBABLY_NOT);
    }

    @Override
    protected String getFirstDisplay() {
        return "**Let's begin!**\nThink of any character/object/animal and answer below."
                + "\n\nAre you thinking of " + (guessTypeIndex == 0 ? "a **" : "an **") + GUESS_TYPES[guessTypeIndex].name().toLowerCase() + "**?"
                + "\n\n:regional_indicator_y: Yes\n:regional_indicator_n: No";
    }

    @Override
    protected void onStart() {
        super.registerReactionListener();
        super.getGameMessage().addReaction(ReactionEmoji.unicode(GameEmoji.Y)).block();
        super.getGameMessage().addReaction(ReactionEmoji.unicode(GameEmoji.N)).block();
        super.getGameMessage().addReaction(ReactionEmoji.unicode(GameEmoji.D)).block();
        super.getGameMessage().addReaction(ReactionEmoji.unicode(GameEmoji.P)).block();
        super.getGameMessage().addReaction(ReactionEmoji.unicode(GameEmoji.U)).block();
        super.getGameMessage().addReaction(ReactionEmoji.unicode(GameEmoji.LEFT_ARROW)).block();
        super.getGameMessage().addReaction(ReactionEmoji.unicode(GameEmoji.EXIT)).block();
    }

    @Override
    protected void onTurn(String input) {
        if (aw == null) {
            handleGuessType(input);
            return;
        }

        if (input.equals(GameEmoji.LEFT_ARROW)) {
            aw.undoAnswer();
            super.setInfoDisplay("");
            return;
        }

        if (currentGuess != null) {
            if (input.equals(GameEmoji.Y)) {
                super.getGameMessage().edit(spec -> spec.setEmbed(embed -> {
                    embed.setAuthor("Akinator \uD83E\uDDDE\u200D♂️", "", getGameMessage().getClient().getSelf().block().getAvatarUrl());
                    embed.setDescription(":grin: I win! Your " + GUESS_TYPES[guessTypeIndex].name().toLowerCase() + " was:\n\n**"
                            + currentGuess.getName() + "**\n" + Optional.ofNullable(currentGuess.getDescription()).orElse(""));
                    embed.setImage(Optional.ofNullable(currentGuess.getImage().toString()).orElse(""));
                    embed.setColor(DiscordColor.GREEN);
                })).block();
                super.end();
                return;
            } else if (input.equals(GameEmoji.N)) {
                declinedGuesses.add(currentGuess.getIdLong());
                currentGuess = null;
            }
        } else {
            aw.answerCurrentQuestion(answerMap.get(input));
        }

        if (aw.getCurrentQuestion() == null || aw.getCurrentQuestion().getProgression() > 85) {
            List<Guess> currentGuesses = aw.getGuesses();
            for (Guess guess : currentGuesses) {
                if (!declinedGuesses.contains(guess.getIdLong()) && (aw.getCurrentQuestion() == null || guess.getProbability() > 0.80)) {
                    sendGuessMessage(guess);
                    currentGuess = guess;
                    return;
                }
            }
        }

        if (aw.getCurrentQuestion() != null) {
            super.setInfoDisplay("");
        } else {
            super.lose(":relieved: Bravo. I have been defeated.");
        }
    }

    private void sendGuessMessage(Guess guess) {
        super.getGameMessage().edit(spec -> spec.setEmbed(embed -> {
            embed.setAuthor("Akinator \uD83E\uDDDE\u200D♂️", "", BotUtils.BOT_AVATAR_URL);
            embed.setDescription(":grinning: I've got it! Is it **" + guess.getName() + "?**\n\n:regional_indicator_y: Yes\n:regional_indicator_n: No");
            embed.setImage(Optional.ofNullable(guess.getImage()).map(URL::toString).orElse(""));
            embed.setColor(DiscordColor.ORANGE);
        })).block();
    }

    private void handleGuessType(String input) {
        if (input.equals(GameEmoji.Y)) {
            try {
                super.setGameDisplay("**Lets see..**");
                aw = new AkiwrapperBuilder().setFilterProfanity(false).setGuessType(GUESS_TYPES[guessTypeIndex]).build();
                super.setInfoDisplay("");
            } catch (ServerNotFoundException e) {
                this.lose("Something went wrong. Akinator is dead.");
            }
        } else if (input.equals(GameEmoji.N)) {
            guessTypeIndex = (guessTypeIndex + 1) % GUESS_TYPES.length; //allows looping back to first element
            super.setGameDisplay("Are you thinking of " + (guessTypeIndex == 0 ? "a **" : "an **")
                    + GUESS_TYPES[guessTypeIndex].name().toLowerCase() + "**?"
                    + "\n\n:regional_indicator_y: Yes\n:regional_indicator_n: No");
        }
    }

    private String getEmojiForProgression() {
        if (aw.getCurrentQuestion().getProgression() < 20) {
            return ":neutral_face:";
        } else if (aw.getCurrentQuestion().getProgression() < 35) {
            return ":face_with_raised_eyebrow:";
        } else if (aw.getCurrentQuestion().getProgression() < 55) {
            return ":thinking:";
        } else if (aw.getCurrentQuestion().getProgression() < 70) {
            return ":open_mouth:";
        } else {
            return ":smirk:";
        }
    }

    @Override
    protected boolean isValidInput(String input) {
        return (currentGuess == null && aw != null) || input.equals(GameEmoji.Y) || input.equals(GameEmoji.N);
    }

    @Override
    protected String getBoard() {
        return getEmojiForProgression() + " **" + aw.getCurrentQuestion().getQuestion() + "**\n\n"
                + ":regional_indicator_y: Yes\n:regional_indicator_n: No\n:regional_indicator_d: Don't know\n"
                + ":regional_indicator_p: Probably\n:regional_indicator_u: Unlikely\n\n:arrow_left: (Undo last answer)";
    }
}
