package discord.util;

import discord.command.utility.TagCommand;
import discord.data.object.user.Prestige;
import discord.data.object.user.Progress;
import discord.data.object.user.DUser;

import java.text.DecimalFormat;
import java.util.List;
import java.util.function.Consumer;

import discord4j.core.object.entity.Role;
import discord4j.core.spec.legacy.LegacyEmbedCreateSpec;

public class ProfileBuilder {

    private final DUser user;
    private final Progress prog;
    private Consumer<LegacyEmbedCreateSpec> embed;

    public ProfileBuilder(DUser user) {
        this.user = user;
        this.prog = user.getProg();
        setupBase();
    }

    private void setupBase() {
        embed = embed -> {
            embed.setAuthor(user.getName().getNick() + " " + user.getName().getEmojis(), "", user.asGuildMember().getAvatarUrl());
            embed.setColor(user.asGuildMember().getColor().block());
            embed.setThumbnail(user.asGuildMember().getAvatarUrl());
        };
    }

    public ProfileBuilder addDesc() {
        embed = embed.andThen(embed -> embed.setDescription("***" + user.getDesc() + "***"));
        return this;
    }

    public ProfileBuilder addRank() {
        embed = embed.andThen(embed -> embed.addField("Rank :trident:", "" + prog.getRank().getName()
                + (prog.getRank().isMax() ? " *(Max)*" : ""), true));
        return this;
    }

    public ProfileBuilder addLevel() {
        embed = embed.andThen(embed -> embed.addField("Level :gem:", "" + prog.getLevel()
                + (prog.isMaxLevel() ? " *(Max)*" : ""), true));
        return this;
    }

    public ProfileBuilder addXPProgress() {
        DecimalFormat formatter = new DecimalFormat("#.###");
        embed = embed.andThen(embed -> embed.addField("XP :diamond_shape_with_a_dot_inside:", ""
                + formatter.format(prog.getXP()) + " / " + prog.getXpTotalForLevelUp() + "", true));
        return this;
    }

    public ProfileBuilder addPrestige() {
        embed = embed.andThen(embed -> embed.addField("Prestige :trophy:", "" + getOrdinal(prog.getPrestige().getNumber()) + ""
                + " (" + prog.getPrestige().getBadge() + ")"
                + (prog.getPrestige().isMax() ? " *(Max)*" : ""), true));
        return this;
    }

    public ProfileBuilder addReincarnation() {
        embed = embed.andThen(embed -> embed.addField("Reincarnation :white_flower:", "" + getOrdinal(prog.getReincarnation().getNumber())
                + " *(" + prog.getReincarnation().getKanji()
                + "-" + prog.getReincarnation().getRomaji() + ")*", true));
        return this;
    }

    public ProfileBuilder addBarProgressToMaxLevel() {
        int currentTotalXP = Progress.getTotalXPToLevel(prog.getLevel()) + (int) prog.getXP();
        int maxXP = Progress.getTotalXPToLevel(Progress.MAX_LEVEL);
        int percentage = (int) Math.floor((double) currentTotalXP / maxXP * 100);
        embed = embed.andThen(embed -> embed.addField(percentage + "% to Max Level :checkered_flag:",
                drawBarProgress(percentage), false));
        return this;
    }

    public ProfileBuilder addTotalLevel() {
        embed = embed.andThen(embed -> embed.addField("Total Level :arrows_counterclockwise:",
                "" + String.format("%,d", prog.getTotalLevel()) + "", true));
        return this;
    }

    public ProfileBuilder addTotalXP() {
        embed = embed.andThen(embed -> embed.addField("Total XP :clock4:", "" + String.format("%,d", prog.getTotalXP()) + "", true));
        return this;
    }

    public ProfileBuilder addBadgeCase() {
        embed = embed.andThen(embed -> embed.addField("Badge Case :beginner: ", getUserBadges(), true));
        return this;
    }

    public ProfileBuilder addXPBoost() {
        embed = embed.andThen(embed -> embed.addField("XP Boost :rocket:", "" + (int) ((prog.getXPMultiplier() - 1) * 100)
                + "% (" + prog.getXPMultiplier() + "x)", true)); //multiplier to percent
        return this;
    }

    public ProfileBuilder addXPRate(int users) {
        embed = embed.andThen(embed -> embed.addField("Current Rate :hourglass_flowing_sand:", "" + prog.getXPRate(users) + " XP/min", true));
        return this;
    }

    public ProfileBuilder addBalance() {
        embed = embed.andThen(embed -> embed.addField("Balance :moneybag:", "$" + String.format("%,d", user.getBalance()) + "", true));
        return this;
    }

    public ProfileBuilder addTags() {
        final StringBuilder sb = new StringBuilder("\uD83C\uDFAE ");
        List<String> roleNames = user.asGuildMember().getRoles()
                .filter(role -> role.getRawPosition() < TagCommand.GAMES_ROLE_POSITION)
                .map(Role::getName).collectList().block();
        if (!roleNames.isEmpty()) {
            for (int i = 0; i < roleNames.size() - 1; i++) {
                sb.append(roleNames.get(i)).append(" – ");
            }
            sb.append(roleNames.get(roleNames.size() - 1));
            embed = embed.andThen(embed -> embed.setFooter(sb.toString(), ""));
        }
        return this;
    }

    public Consumer<LegacyEmbedCreateSpec> build() {
        return embed;
    }

    private String getUserBadges() {
        String badges = "";
        for (int i = 1; i <= prog.getPrestige().getNumber(); i++) {
            badges += Prestige.BADGES[i];
        }
        return badges;
    }

    private String drawBarProgress(int percentage) {
        StringBuilder sb = new StringBuilder();
        //generate an int 1-10 depicting prog based on percentage
        int prog = percentage / 10;
        for (int i = 1; i <= 10; i++) {
            if (i <= prog)
                sb.append("\u2B1C"); //white square emoji
            else
                sb.append("\u2B1B"); //black square emoji
        }
        return sb.toString();
    }

    //we only have to support up to max reincarnation (20)
    public static String getOrdinal(int cardinal) {
        if (cardinal == 1) return "1st";
        else if (cardinal == 2) return "2nd";
        else if (cardinal == 3) return "3rd";
        else return cardinal + "th";
    }
}
