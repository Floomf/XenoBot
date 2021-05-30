package discord.command.fun;

import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.core.command.InteractionContext;
import discord.util.BotUtils;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.util.ApplicationCommandOptionType;
import kong.unirest.Unirest;

public class AnimalCommand extends AbstractCommand {

    public AnimalCommand() {
        super(new String[]{"animal"}, 0, CommandCategory.FUN);
    }

    @Override
    public ApplicationCommandRequest buildSlashCommand() {
        return ApplicationCommandRequest.builder()
                .name("animal")
                .description("View a random animal image")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("name")
                        .description("Animal name")
                        .type(ApplicationCommandOptionType.STRING.getValue())
                        .required(true)
                        .addChoice(ApplicationCommandOptionChoiceData.builder().name("Bird").value("bird").build())
                        .addChoice(ApplicationCommandOptionChoiceData.builder().name("Bunny").value("bunny").build())
                        .addChoice(ApplicationCommandOptionChoiceData.builder().name("Cat").value("cat").build())
                        .addChoice(ApplicationCommandOptionChoiceData.builder().name("Dog").value("dog").build())
                        .addChoice(ApplicationCommandOptionChoiceData.builder().name("Duck").value("duck").build())
                        .addChoice(ApplicationCommandOptionChoiceData.builder().name("Fox").value("fox").build())
                        .addChoice(ApplicationCommandOptionChoiceData.builder().name("Lizard").value("lizard").build())
                        .addChoice(ApplicationCommandOptionChoiceData.builder().name("Koala").value("koala").build())
                        .addChoice(ApplicationCommandOptionChoiceData.builder().name("Monkey").value("monkey").build())
                        .addChoice(ApplicationCommandOptionChoiceData.builder().name("Panda").value("panda").build())
                        .build())
                .build();
    }

    @Override
    public void execute(InteractionContext context) {
        context.reply(embed -> {
            embed.setImage(getImageLinkForAnimal(context.getOptionAsString("name")));
            embed.setColor(BotUtils.getRandomColor());
        });
    }

    public String getImageLinkForAnimal(String animal) {
        switch (animal) {
            case "bird":
                return Unirest.get("https://some-random-api.ml/img/birb")
                        .asJson().getBody().getObject().getString("link");
            case "bunny":
                return Unirest.get("https://api.bunnies.io/v2/loop/random/?media=gif")
                        .asJson().getBody().getObject().getJSONObject("media").getString("gif");
            case "cat":
                return Unirest.get("https://api.thecatapi.com/v1/images/search")
                        .header("x-api-key", "82ac98b9-6bc2-4e04-9401-9949905f1f92") //shhh
                        .asJson().getBody().getArray().getJSONObject(0).getString("url");
            case "dog":
                return Unirest.get("https://dog.ceo/api/breeds/image/random")
                        .asJson().getBody().getObject().getString("message");
            case "fox":
                return Unirest.get("https://randomfox.ca/floof/")
                        .asJson().getBody().getObject().getString("image");
            case "duck":
                return Unirest.get("https://random-d.uk/api/random")
                        .asJson().getBody().getObject().getString("url");
            case "panda":
                return Unirest.get("https://some-random-api.ml/img/panda")
                        .asJson().getBody().getObject().getString("link");
            case "koala":
                return Unirest.get("https://some-random-api.ml/img/koala")
                        .asJson().getBody().getObject().getString("link");
            case "monkey":
                int length = (int) (Math.random() * 200) + 800;
                int height = (int) (Math.random() * 200) + 600;
                return "https://www.placemonkeys.com/" + length + "/" + height + "?random";
            case "lizard":
                return Unirest.get("https://nekos.life/api/v2/img/lizard")
                        .asJson().getBody().getObject().getString("url");
        }
        return "";
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
