package discord.command.fun;

import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.util.BotUtils;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.TextChannel;
import kong.unirest.Unirest;

public class DogCommand extends AbstractCommand {

    public DogCommand() {
        super(new String[]{"dog", "doggy", "bitch"}, 0, CommandCategory.FUN);
    }

    @Override
    public void execute(Message message, TextChannel channel, String[] args) {
        channel.createEmbed(embed -> embed.setImage(
                Unirest.get("https://api.thedogapi.com/v1/images/search")
                        .header("x-api-key", "b0686b3c-1b19-448a-812a-a04c7ca7124d") //shhh
                        .asJson().getBody().getArray().getJSONObject(0).getString("url"))).block();
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "", "View a random dog.");
    }

}
