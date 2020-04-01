package discord.command.game.math;

import discord.core.game.AbstractGame;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.TextChannel;
import discord.util.BotUtils;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.core.game.GameManager;
import discord4j.core.object.entity.Message;

public class MathCommand extends AbstractCommand {

    public MathCommand() {
        super(new String[]{"math"}, 0, CommandCategory.GAME);
    }

    @Override //TODO hacky and bad
    public void execute(Message message, TextChannel channel, String[] args) {
        Message gameMessage = channel.createMessage(spec -> spec.setEmbed(embed -> embed.setDescription("Ready..."))).block();
        try {
            Thread.sleep(2000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        AbstractGame game = new GameMath(gameMessage, new Member[] {message.getAuthorAsMember().block(), message.getAuthorAsMember().block()});
        GameManager.addGame(message, game);
        game.start();
        //GameManager.processGameCommand(message, channel, "Quick Math", GameMath.class);
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "", "Play a game of Quick Math.");
    }

}

