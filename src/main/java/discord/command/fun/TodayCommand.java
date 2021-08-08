package discord.command.fun;

import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.core.command.InteractionContext;
import discord.util.BotUtils;
import discord.util.MessageUtils;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.util.ApplicationCommandOptionType;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;

import java.time.LocalDate;

public class TodayCommand extends AbstractCommand {

    public TodayCommand() {
        super("today", 0, CommandCategory.FUN);
    }

    @Override
    public ApplicationCommandRequest buildSlashCommand() {
        return ApplicationCommandRequest.builder()
                .name(getName())
                .description("View a random fact about today in history")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("event")
                        .description("View a random event that occurred today in history")
                        .type(ApplicationCommandOptionType.SUB_COMMAND.getValue())
                        .build())
                .addOption(ApplicationCommandOptionData.builder()
                        .name("birthday")
                        .description("View a random birth that occurred today in history")
                        .type(ApplicationCommandOptionType.SUB_COMMAND.getValue())
                        .build())
                .addOption(ApplicationCommandOptionData.builder()
                        .name("death")
                        .description("View a random death that occurred today in history")
                        .type(ApplicationCommandOptionType.SUB_COMMAND.getValue())
                        .build())
                .build();
    }

    @Override
    public void execute(InteractionContext context) {
        String type = context.getSubCommand().getName();
        LocalDate today = LocalDate.now();

        JSONObject jsonHistory = Unirest.get("http://history.muffinlabs.com/date/" + today.getMonthValue() + "/" + today.getDayOfMonth())
                .asJson().getBody().getObject();

        String jsonArrayToFetch = "";
        String prefix = "";
        if (type.equals("event")) {
            jsonArrayToFetch = "Events";
        } else if (type.equals("birthday")) {
            jsonArrayToFetch = "Births";
            prefix = "Born on ";
        } else if (type.equals("death")) {
            jsonArrayToFetch = "Deaths";
            prefix = "Died on ";
        }

        JSONObject jsonRandomDate = jsonHistory.getJSONObject("data").getJSONArray(jsonArrayToFetch).getJSONObject(
                (int) (Math.random() * jsonHistory.getJSONObject("data").getJSONArray(jsonArrayToFetch).length()));
        context.reply(MessageUtils.getEmbed(prefix + jsonHistory.getString("date") + ", " + jsonRandomDate.getInt("year"),
                jsonRandomDate.getString("text"), BotUtils.getRandomColor()));
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
