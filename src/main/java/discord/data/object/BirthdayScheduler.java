package discord.data.object;

import discord.data.UserManager;
import discord.data.object.user.DUser;
import discord.util.BotUtils;
import discord.util.MessageUtils;
import discord.util.ProfileBuilder;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.TextChannel;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BirthdayScheduler {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Snowflake birthdayRole;
    private final TextChannel announceChannel;

    private DUser birthdayUser = null;

    public BirthdayScheduler(Guild guild) {
        announceChannel = guild.getChannels().ofType(TextChannel.class)
                .filter(channel -> channel.getName().equals("general")).blockFirst();
        birthdayRole = guild.getRoles().filter(role -> role.getName().equals("Birthday Boy \uD83E\uDD73")).blockFirst().getId();

        //scheduler.scheduleAtFixedRate(this::checkBirthdays, 1, TimeUnit.DAYS.toMinutes(1), TimeUnit.MINUTES);

        System.out.println("Starting BirthdayScheduler..");
        scheduler.scheduleAtFixedRate(this::checkBirthdays,
                LocalDateTime.now().until(LocalDate.now().plusDays(1).atStartOfDay().plusMinutes(1),
                        ChronoUnit.MINUTES), TimeUnit.DAYS.toMinutes(1), TimeUnit.MINUTES);

        //scheduler.scheduleAtFixedRate(this::checkBirthdays, 0, TimeUnit.DAYS.toMinutes(1), TimeUnit.MINUTES);
    }

    private void checkBirthdays() {
        if (birthdayUser != null) { //null checks?
            birthdayUser.asGuildMember().removeRole(birthdayRole).block();
            birthdayUser = null;
        }

        for (DUser dUser : UserManager.getDUsers()) {
            int[] birthday = dUser.getBirthday();
            if (birthday != null && birthday[0] == LocalDate.now().getMonthValue()
                    && birthday[1] == LocalDate.now().getDayOfMonth()) {
                onBirthday(dUser);
                return;
            }
        }
    }

    private void onBirthday(DUser dUser) {
        birthdayUser = dUser;
        birthdayUser.asGuildMember().addRole(birthdayRole).block();
        int age = LocalDate.now().getYear() - dUser.getBirthday()[2];

        String kingRoleMention = dUser.asGuildMember().getGuild().block().getRoleById(Snowflake.of(387905623662133249L)).block().getMention();
        String homieRoleMention = dUser.asGuildMember().getGuild().block().getRoleById(Snowflake.of(748096885486780518L)).block().getMention();

        announceChannel.createMessage(spec -> {
            spec.setContent(kingRoleMention + " " + homieRoleMention);
            spec.setEmbed(MessageUtils.getEmbed("We've got a birthday today! \uD83C\uDF89",
                    "**HAPPY " + age / 10 + ProfileBuilder.getOrdinal(age % 10).toUpperCase() + " BIRTHDAY\n"
                            + birthdayUser.asGuildMember().getMention() + "!**\n\nHave an amazing day! "
                            + BotUtils.getGuildEmojiString(announceChannel.getGuild().block(),
                            "FeelsBirthdayMan"), dUser.asGuildMember().getColor().block())
                    .andThen(embed -> embed.setThumbnail(dUser.asGuildMember().getAvatarUrl())));
        }).block();
    }

}
