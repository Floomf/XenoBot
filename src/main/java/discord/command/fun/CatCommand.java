package discord.command.fun;

import discord4j.core.object.entity.channel.TextChannel;
import kong.unirest.Unirest;
import discord.util.BotUtils;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;

import discord4j.core.object.entity.Message;

public class CatCommand extends AbstractCommand {

    public CatCommand() {
        super(new String[]{"cat", "kitty", "pussy"}, 0, CommandCategory.FUN);
    }

    @Override
    public void execute(Message message, TextChannel channel, String[] args) {
        channel.createEmbed(embed -> embed.setImage(
                Unirest.get("https://api.thecatapi.com/v1/images/search")
                        .header("x-api-key", "82ac98b9-6bc2-4e04-9401-9949905f1f92") //shhh
                        .asJson().getBody().getArray().getJSONObject(0).getString("url"))).block();
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "", "View a random cat.");
    }

    @Override
    public boolean isSupportedGlobally() {
        return true;
    }

}
