package discord.core.command;

import discord.data.object.user.DUser;
import discord.manager.UserManager;
import discord.util.MessageUtils;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.component.LayoutComponent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionReplyEditSpec;
import discord4j.core.spec.legacy.LegacyEmbedCreateSpec;
import discord4j.discordjson.json.*;
import discord4j.rest.util.AllowedMentions;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;


public class InteractionContext {

    public ChatInputInteractionEvent event;

    public InteractionContext(ChatInputInteractionEvent event) {
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
        return event.getOptions().get(0);
    }

    public List<ApplicationCommandInteractionOption> getOptions() {
        return event.getOptions();
    }

    public Optional<ApplicationCommandInteractionOption> getOption(String name) {
        return event.getOption(name);
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

    public void deferReply() {
        event.deferReply().block();
    }

    public void editReply(EmbedCreateSpec embed) {
        event.editReply(InteractionReplyEditSpec.create().withEmbeds(embed)).block();
    }

    public void reply(String content, Consumer<LegacyEmbedCreateSpec> embed, LayoutComponent[] components, boolean isEphemeral) {
        event.reply(spec -> {
            spec.setContent(content);
            if (embed != null) { //bad?
                spec.addEmbed(embed);
            }
            spec.setComponents(components);
            spec.setEphemeral(isEphemeral);
        }).doOnError(Throwable::printStackTrace).onErrorResume(e -> Mono.empty()).block();
    }

    public void reply(String content, Consumer<LegacyEmbedCreateSpec> embed, LayoutComponent[] components) {
        reply(content, embed, components, false);
    }

    public void reply(String content, Consumer<LegacyEmbedCreateSpec> embed) {
        reply(content, embed, new LayoutComponent[0]);
    }

    public void reply(Consumer<LegacyEmbedCreateSpec> embed) {
        reply(null, embed);
    }

    public void replyWithInfo(String info) {
        reply(MessageUtils.getInfoEmbed(info));
    }

    //All error messages are private to the user
    public void replyWithError(String error) {
        replyEphemeral(MessageUtils.getErrorEmbed(error));
    }

    public void replyEphemeral(Consumer<LegacyEmbedCreateSpec> embed) {
        reply(null, embed, new LayoutComponent[0], true);
    }

    public void replyEphemeral(String content) {
        reply(content, null, new LayoutComponent[0], true);
    }

    public Message edit(String content, Consumer<LegacyEmbedCreateSpec> embedSpec) {
        LegacyEmbedCreateSpec spec = new LegacyEmbedCreateSpec();
        embedSpec.accept(spec);
        MessageData data = event.getInteractionResponse().editInitialResponse(WebhookMessageEditRequest.builder()
                .embeds(List.of(spec.asRequest()))
                .content(content)
                .allowedMentions(AllowedMentions.builder().parseType(AllowedMentions.Type.USER).build().toData())
                .build())
                .block();
        return event.getClient().getMessageById(getChannel().getId(), Snowflake.of(data.id())).block();
    }

    public Message edit(Consumer<LegacyEmbedCreateSpec> embedSpec) {
        LegacyEmbedCreateSpec spec = new LegacyEmbedCreateSpec();
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

    public void createFollowupMessageEphemeral(String content) {
        event.getInteractionResponse().createFollowupMessageEphemeral(content).block();
    }

}
