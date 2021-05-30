package discord.command.fun;

import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.core.command.InteractionContext;
import discord.util.BotUtils;
import discord.util.DiscordColor;
import discord.util.MessageUtils;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.util.ApplicationCommandOptionType;
import kong.unirest.Unirest;

public class EightBallCommand extends AbstractCommand {

    public EightBallCommand() {
        super(new String[]{"8ball"}, 0, CommandCategory.FUN);
    }

    @Override
    public ApplicationCommandRequest buildSlashCommand() {
        return ApplicationCommandRequest.builder()
                .name("8ball")
                .description("Ask a yes–no question to the Magic 8-Ball")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("question")
                        .description("Your question")
                        .type(ApplicationCommandOptionType.STRING.getValue())
                        .required(true)
                        .build())
                .build();
    }

    @Override
    public void execute(InteractionContext context) {
        String question = context.getOptionAsString("question");
        question = question.charAt(question.length() - 1) == '?' ? question : question + "?";
        context.reply(MessageUtils.getEmbed(question, Unirest.get("https://8ball.delegator.com/magic/JSON/" + question)
                        .asJson().getBody().getObject().getJSONObject("magic").getString("answer") + ".", DiscordColor.PURPLE));
    }

    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "", "");
    }

    @Override
    public boolean isSupportedGlobally() {
        return true;
    }

}

