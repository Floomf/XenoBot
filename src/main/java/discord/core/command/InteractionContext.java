package discord.core.command;

import discord.data.object.user.DUser;
import discord.manager.UserManager;
import discord.util.MessageUtils;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.InteractionCreateEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.discordjson.json.AllowedMentionsData;
import discord4j.discordjson.json.EmbedData;
import discord4j.discordjson.json.MessageData;
import discord4j.discordjson.json.WebhookMessageEditRequest;
import discord4j.rest.util.AllowedMentions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;


public class InteractionContext {

    public InteractionCreateEvent event;

    public InteractionContext(InteractionCreateEvent event) {
        this.event = event;
    }

    public Member getMember() {
        return event.getInteraction().getMember().get();
    }

    public DUser getDUser() {
        return UserManager.getDUserFromUser(event.getInteraction().getUser());
    }

    public Guild getGuild() {
        return event.getInteraction().getGuild().block();
    }

    public TextChannel getChannel() {
        return event.getInteraction().getChannel().cast(TextChannel.class).block();
    }

    public ApplicationCommandInteractionOption getSubCommand() {
        return event.getInteraction().getCommandInteraction().getOptions().get(0);
    }

    public Optional<ApplicationCommandInteractionOption> getOption(String name) {
        return event.getInteraction().getCommandInteraction().getOption(name);
    }

    public String getOptionAsString(String name) {
        return getOption(name)
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::getRaw)
                .orElse("");
    }

    public Optional<Member> getOptionAsMember(String name) {
        return getOption(name)
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asSnowflake)
                .map(value -> getGuild().getMemberById(value).block());
    }

    public Optional<User> getOptionAsUser(String name) {
        return getOption(name)
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(value -> value.asUser().block());
    }

    public Optional<Long> getOptionAsLong(String name) {
        return getOption(name)
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asLong);
    }

    public Optional<Boolean> getOptionAsBoolean(String name) {
        return getOption(name)
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asBoolean);
    }

    public void acknowledge() {
        event.acknowledge().block();
    }

    public void reply(String content, Consumer<EmbedCreateSpec> embed) {
        event.reply(spec -> {
            spec.setContent(content);
            spec.addEmbed(embed);
        }).block();
    }

    public void reply(Consumer<EmbedCreateSpec> embed) {
        reply(null, embed);
    }

    public void replyWithInfo(String info) {
        reply(MessageUtils.getInfoEmbed(info));
    }

    public void replyWithError(String error) {
        reply(MessageUtils.getErrorEmbed(error));
    }

    public void replyEphemeral(String content) {
        event.replyEphemeral(content).block();
    }

    public Message edit(String content, Consumer<EmbedCreateSpec> embedSpec) {
        EmbedCreateSpec spec = new EmbedCreateSpec();
        embedSpec.accept(spec);
        MessageData data = event.getInteractionResponse().editInitialResponse(WebhookMessageEditRequest.builder()
                .embeds(List.of(spec.asRequest()))
                .content(content)
                .allowedMentions(AllowedMentions.builder().parseType(AllowedMentions.Type.USER).build().toData())
                .build())
                .block();
        return event.getClient().getMessageById(getChannel().getId(), Snowflake.of(data.id())).block();
    }

    public Message edit(Consumer<EmbedCreateSpec> embedSpec) {
        EmbedCreateSpec spec = new EmbedCreateSpec();
        embedSpec.accept(spec);
        MessageData data = event.getInteractionResponse().editInitialResponse(WebhookMessageEditRequest.builder()
                .embeds(List.of(spec.asRequest())).build())
                .block();
        return event.getClient().getMessageById(getChannel().getId(), Snowflake.of(data.id())).block();
    }

    public Message edit(String content) {
        MessageData data = event.getInteractionResponse().editInitialResponse(WebhookMessageEditRequest.builder()
                .content(content).build())
                .block();
        return event.getClient().getMessageById(getChannel().getId(), Snowflake.of(data.id())).block();
    }

}
