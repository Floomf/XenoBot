package discord.command.game.math;

import discord.core.game.AbstractGame;
import discord4j.core.object.entity.Member;
import discord.util.BotUtils;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.core.game.GameManager;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;

public class MathCommand extends AbstractCommand {

    public MathCommand() {
        super(new String[]{"math"}, 0, CommandCategory.GAME);
    }

    @Override
    public void execute(Message message, TextChannel channel, String[] args) {
        Message gameMessage = channel.createEmbed(embed -> embed.setDescription("Ready..")).block();
        try {
            Thread.sleep(1500L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        AbstractGame game = new GameMath(gameMessage, new Member[] {message.getAuthorAsMember().block()});
        GameManager.addGame(gameMessage, game);
        game.start();
        //GameManager.processGameCommand(message, channel, "Quick Math", GameMath.class);
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "", "Play a game of Quick Math and earn money.");
    }

}

