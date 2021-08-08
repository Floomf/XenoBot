package discord.command.game.akinator;

import com.markozajc.akiwrapper.Akiwrapper;
import com.markozajc.akiwrapper.AkiwrapperBuilder;
import com.markozajc.akiwrapper.core.entities.Guess;
import com.markozajc.akiwrapper.core.entities.Server;
import com.markozajc.akiwrapper.core.exceptions.ServerNotFoundException;
import discord.core.game.SingleplayerGame;
import discord.util.BotUtils;
import discord.util.DiscordColor;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.component.LayoutComponent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.TextChannel;

import java.net.URL;
import java.util.*;

public class GameAkinator extends SingleplayerGame {

    private Akiwrapper aw;
    private final List<Long> declinedGuesses = new ArrayList<>();
    private Guess currentGuess;
    private boolean onlyYesNoButtons = true;

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
    protected LayoutComponent[] getComponents() {
        if (onlyYesNoButtons) {
            return new LayoutComponent[]{
                    ActionRow.of(Button.primary(Akiwrapper.Answer.YES.name(), "Yes"),
                            Button.primary(Akiwrapper.Answer.NO.name(), "No"),
                            Button.secondary("forfeit", "Exit"))
            };
        } else {
            return new LayoutComponent[]{
                    ActionRow.of(Button.primary(Akiwrapper.Answer.YES.name(), "Yes"),
                            Button.primary(Akiwrapper.Answer.NO.name(), "No"),
                            Button.primary(Akiwrapper.Answer.DONT_KNOW.name(), "Don't know"),
                            Button.primary(Akiwrapper.Answer.PROBABLY.name(), "Probably"),
                            Button.primary(Akiwrapper.Answer.PROBABLY_NOT.name(), "Unlikely")),
                    ActionRow.of(Button.secondary("undo", "Undo last answer"),
                            Button.secondary("forfeit", "Exit"))
            };
        }
    }

    @Override
    protected void setup() {

    }

    @Override
    protected String getFirstDisplay() {
        return "**Let's begin!**\nThink of any character/object/animal and answer below."
                + "\n\nAre you thinking of " + (guessTypeIndex == 0 ? "a **" : "an **")
                + GUESS_TYPES[guessTypeIndex].name().toLowerCase() + "**?";
    }

    @Override
    protected void onStart() {
        super.registerComponentListener();
    }

    @Override
    protected void onTurn(String input) {
        if (aw == null) {
            handleGuessType(input);
            return;
        }

        if (input.equals("undo")) {
            aw.undoAnswer();
            super.setInfoDisplay("");
            return;
        }

        if (currentGuess != null) {
            if (input.equals(Akiwrapper.Answer.YES.name())) {
                super.getGameMessage().edit(spec -> {
                    spec.addEmbed(embed -> {
                        embed.setAuthor("Akinator \uD83E\uDDDE\u200D♂️", "", getGameMessage().getClient().getSelf().block().getAvatarUrl());
                        embed.setDescription(":grin: I win! Your " + GUESS_TYPES[guessTypeIndex].name().toLowerCase() + " was:\n\n**"
                                + currentGuess.getName() + "**\n" + Optional.ofNullable(currentGuess.getDescription()).orElse(""));
                        embed.setImage(Optional.ofNullable(currentGuess.getImage().toString()).orElse(""));
                        embed.setColor(DiscordColor.GREEN);
                    });
                    spec.setComponents();
                }).block();
                super.end();
                return;
            } else if (input.equals(Akiwrapper.Answer.NO.name())) {
                declinedGuesses.add(currentGuess.getIdLong());
                currentGuess = null;
                onlyYesNoButtons = false;
            }
        } else {
            aw.answerCurrentQuestion(Akiwrapper.Answer.valueOf(input));
        }

        if (aw.getCurrentQuestion() == null || aw.getCurrentQuestion().getProgression() > 85) {
            List<Guess> currentGuesses = aw.getGuesses();
            for (Guess guess : currentGuesses) {
                if (!declinedGuesses.contains(guess.getIdLong()) && (aw.getCurrentQuestion() == null || guess.getProbability() > 0.80)) {
                    onlyYesNoButtons = true;
                    currentGuess = guess;
                    editMessageToGuess(guess);
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

    private void editMessageToGuess(Guess guess) {
        super.componentEvent.edit(spec -> {
            spec.addEmbed(embed -> {
                embed.setAuthor("Akinator \uD83E\uDDDE\u200D♂️", "", BotUtils.BOT_AVATAR_URL);
                embed.setDescription(":grinning: I've got it! Is it **" + guess.getName() + "?**");
                embed.setImage(Optional.ofNullable(guess.getImage()).map(URL::toString).orElse(""));
                embed.setColor(DiscordColor.ORANGE);
            });
            spec.setComponents(getComponents());
        }).block();
    }

    private void handleGuessType(String input) {
        if (input.equals(Akiwrapper.Answer.YES.name())) {
            try {
                super.setGameDisplay("**Let me think..**");
                aw = new AkiwrapperBuilder().setGuessType(GUESS_TYPES[guessTypeIndex]).build();
                onlyYesNoButtons = false;
                super.setInfoDisplay("");
            } catch (ServerNotFoundException e) {
                this.lose("Something went wrong. Akinator is dead.");
            }
        } else if (input.equals(Akiwrapper.Answer.NO.name())) {
            guessTypeIndex = (guessTypeIndex + 1) % GUESS_TYPES.length; //allows looping back to first element
            super.setGameDisplay("Are you thinking of " + (guessTypeIndex == 0 ? "a **" : "an **")
                    + GUESS_TYPES[guessTypeIndex].name().toLowerCase() + "**?");
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
        return (currentGuess == null && aw != null) || input.equals(Akiwrapper.Answer.YES.name()) || input.equals(Akiwrapper.Answer.NO.name());
    }

    @Override
    protected String getBoard() {
        return getEmojiForProgression() + " **__Question " + (aw.getCurrentQuestion().getStep() + 1) + "__**\n" + aw.getCurrentQuestion().getQuestion();

                /*+ "**\n\n"
                + ":regional_indicator_y: Yes\n:regional_indicator_n: No\n:regional_indicator_d: Don't know\n"
                + ":regional_indicator_p: Probably\n:regional_indicator_u: Unlikely\n\n:arrow_left: (Undo last answer)";*/
    }
}
