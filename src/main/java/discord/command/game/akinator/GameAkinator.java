package discord.command.game.akinator;

import com.markozajc.akiwrapper.Akiwrapper;
import com.markozajc.akiwrapper.AkiwrapperBuilder;
import com.markozajc.akiwrapper.core.entities.Guess;
import com.markozajc.akiwrapper.core.entities.Server;
import com.markozajc.akiwrapper.core.exceptions.ServerNotFoundException;
import discord.core.game.Button;
import discord.core.game.ButtonGame;
import discord.util.BotUtils;
import discord.util.DiscordColor;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;

import java.net.URL;
import java.util.*;

public class GameAkinator extends ButtonGame {

    private Akiwrapper aw;
    private final HashMap<Button, Akiwrapper.Answer> answerMap = new HashMap<>();
    private final List<Long> declinedGuesses = new ArrayList<>();
    private Guess currentGuess;

    private final static Server.GuessType[] GUESS_TYPES = {Server.GuessType.CHARACTER, Server.GuessType.OBJECT, Server.GuessType.ANIMAL};
    private int guessTypeIndex = 0;

    public GameAkinator(Message message, Member[] players, int betAmount) {
        super(message, players, 0);

        super.getButtonManager().addButton(message, Button.Y);
        super.getButtonManager().addButton(message, Button.N);
        super.getButtonManager().addButton(message, Button.D);
        super.getButtonManager().addButton(message, Button.P);
        super.getButtonManager().addButton(message, Button.U);
        super.getButtonManager().addButton(message, Button.LEFT_ARROW);

        super.getButtonManager().addButton(message, Button.EXIT);

        answerMap.put(Button.Y, Akiwrapper.Answer.YES);
        answerMap.put(Button.N, Akiwrapper.Answer.NO);
        answerMap.put(Button.D, Akiwrapper.Answer.DONT_KNOW);
        answerMap.put(Button.P, Akiwrapper.Answer.PROBABLY);
        answerMap.put(Button.U, Akiwrapper.Answer.PROBABLY_NOT);
    }

    @Override
    protected String getGameTitle() {
        return "Akinator \uD83E\uDDDE\u200D♂️";
    }

    @Override
    protected String getForfeitMessage(Member forfeiter) {
        return "See you next time!";
    }

    @Override
    protected String getIdleMessage(Member idler) {
        return "Where did you go? See you later.";
    }

    @Override
    protected void onStart() {
        super.setGameDisplay("**Let's begin!**\nThink of any character/object/animal and answer below."
                + "\n\nAre you thinking of " + (guessTypeIndex == 0 ? "a **" : "an **") + GUESS_TYPES[guessTypeIndex].name().toLowerCase() + "**?"
                + "\n\n:regional_indicator_y: Yes\n:regional_indicator_n: No");
    }

    @Override
    protected void onTurn(Button input) {
        if (aw == null) {
            handleGuessType(input);
            return;
        }

        if (input == Button.LEFT_ARROW) {
            aw.undoAnswer();
            super.setInfoDisplay("");
            return;
        }

        if (currentGuess != null) {
            if (input == Button.Y) {
                super.getGameMessage().edit(spec -> spec.setEmbed(embed -> {
                    embed.setAuthor(getGameTitle(), "", getGameMessage().getClient().getSelf().block().getAvatarUrl());
                    embed.setDescription(":grin: I win! Your " + GUESS_TYPES[guessTypeIndex].name().toLowerCase() + " was:\n\n**"
                            + currentGuess.getName() + "**\n" + Optional.ofNullable(currentGuess.getDescription()).orElse(""));
                    embed.setImage(Optional.ofNullable(currentGuess.getImage().toString()).orElse(""));
                    embed.setColor(DiscordColor.GREEN);
                })).block();
                super.end();
                return;
            } else if (input == Button.N) {
                declinedGuesses.add(currentGuess.getIdLong());
                currentGuess = null;
            }
        } else {
            aw.answerCurrentQuestion(answerMap.get(input));
            System.out.println(aw.getCurrentQuestion() == null);
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
            embed.setAuthor(getGameTitle(), "", BotUtils.BOT_AVATAR_URL);
            embed.setDescription(":grinning: I've got it! Is it **" + guess.getName() + "?**\n\n:regional_indicator_y: Yes\n:regional_indicator_n: No");
            embed.setImage(Optional.ofNullable(guess.getImage()).map(URL::toString).orElse(""));
            embed.setColor(DiscordColor.ORANGE);
        })).block();
    }

    private void handleGuessType(Button input) {
        if (input == Button.Y) {
            try {
                super.setGameDisplay("**Lets see..**");
                aw = new AkiwrapperBuilder().setFilterProfanity(false).setGuessType(GUESS_TYPES[guessTypeIndex]).build();
                super.setInfoDisplay("");
            } catch (ServerNotFoundException e) {
                this.lose("Something went wrong. Akinator is dead.");
            }
        } else if (input == Button.N) {
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
    protected boolean isValidInput(Button input) {
        return (currentGuess == null && aw != null) || input.equals(Button.Y) || input.equals(Button.N);
    }

    @Override
    protected String getBoard() {
        return getEmojiForProgression() + " **" + aw.getCurrentQuestion().getQuestion() + "**\n\n"
                + ":regional_indicator_y: Yes\n:regional_indicator_n: No\n:regional_indicator_d: Don't know\n"
                + ":regional_indicator_p: Probably\n:regional_indicator_u: Unlikely\n\n:arrow_left: (Undo last answer)";
    }
}
