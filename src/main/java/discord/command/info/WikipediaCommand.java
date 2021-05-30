package discord.command.info;

import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.core.command.InteractionContext;
import discord.util.BotUtils;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.util.ApplicationCommandOptionType;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;


public class WikipediaCommand extends AbstractCommand {

    public WikipediaCommand() {
        super(new String[]{"wikipedia"}, 1, CommandCategory.INFO);
    }

    @Override
    public ApplicationCommandRequest buildSlashCommand() {
        return ApplicationCommandRequest.builder()
                .name("wikipedia")
                .description("Search for a Wikipedia page")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("query")
                        .description("What to search for")
                        .type(ApplicationCommandOptionType.STRING.getValue())
                        .required(true)
                        .build())
                .build();
    }

    @Override
    public void execute(InteractionContext context) {
        JSONArray search = Unirest.get("https://en.wikipedia.org/w/api.php?action=opensearch&limit=1&search=" + context.getOptionAsString("query"))
                .asJson().getBody().getArray();
        if (search.getJSONArray(1).isEmpty()) {
            context.replyWithError("Couldn't find any page from that query!");
        } else {
            String title = search.getJSONArray(1).getString(0);
            String link = search.getJSONArray(3).getString(0);

            JSONObject request = Unirest.get("https://en.wikipedia.org/w/api.php?action=query&prop=extracts%7Ccategories&clcategories=Category:All%20disambiguation%20pages" +
                    "&explaintext=1&exsectionformat=plain&redirects=true&format=json&titles=" + title).asJson().getBody().getObject()
                    .getJSONObject("query").getJSONObject("pages");
            JSONObject page = request.getJSONObject(request.keys().next());

            boolean isDisambPage = page.has("categories"); //we want all content from disamiguation page
            String text = page.getString("extract");
            System.out.println(isDisambPage);
            if (isDisambPage) {
                System.out.println(text);
                text = text.replaceAll("\\n\\n\\n(.+)\\n\\n\\n", "\n**__$1__**\n\n\n");
                text = text.replaceAll("\\n\\n\\n(.+)\\n", "\n**$1:**\n");
                text = text.substring(0, Math.min(text.length(), 2048));

                int count = 0;
                for (int i = 0; i < text.length(); i++) {
                    if (text.charAt(i) == '\n') {
                        count++;
                        if (count == 50) {
                            text = text.substring(0, i);
                            break;
                        }
                    }
                }
            } else {
                int endIntroIndex = text.indexOf("\n\n\n");
                text = text.substring(0, Math.min(endIntroIndex == -1 ? text.length() : endIntroIndex, 2040)).replace("\n", "\n\n");
                text = text.substring(0, text.lastIndexOf('.') + 1); //TODO may somehow break
            }

            String formatted = text;

            context.reply(spec -> {
                spec.setThumbnail("");
                spec.setAuthor(title, link, BotUtils.BOT_AVATAR_URL);
                spec.setDescription(formatted);
                spec.setColor(BotUtils.getRandomColor());
            });
        }

    }

    @Override
    public String getUsage(String alias) {
        return null;
    }

    @Override
    public boolean isSupportedGlobally() {
        return true;
    }

}
