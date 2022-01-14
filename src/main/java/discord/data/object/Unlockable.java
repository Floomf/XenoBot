package discord.data.object;

import discord.data.object.user.DUser;
import discord.data.object.user.Pref;
import discord.data.object.user.Progress;
import discord.util.BotUtils;
import discord.util.DiscordColor;
import discord.util.MessageUtils;

public class Unlockable {

    enum Type {
        COMMAND, NAME_COLOR, BADGE
    }

    private final Type type;
    private final String name;
    private final int prestige;
    private final int level;

    private Unlockable(Type type, String name, int prestige, int level) {
        this.type = type;
        this.name = name;
        this.prestige = prestige;
        this.level = level;
    }

    public static Unlockable badge(String badge, int prestige, int level) {
        return new Unlockable(Type.BADGE, badge, prestige, level);
    }

    public static Unlockable color(String color, int prestige, int level) {
        return new Unlockable(Type.NAME_COLOR, color, prestige, level);
    }

    public static Unlockable command(String command, int prestige, int level) {
        return new Unlockable(Type.COMMAND, command, prestige, level);
    }

    public void onUnlock(DUser user) {
        //ideally avoid if statements but this is cleaner than having the code in Progress
        if (type == Type.COMMAND && user.getPrefs().get(Pref.NOTIFY_UNLOCK)) {
            MessageUtils.sendMessage(user.asGuildMember().getPrivateChannel().block(), "Command Unlocked!",
            "You've unlocked the ability to use the command `" + name + "` on The Realm!\n\nType it on the server to get started.",
                    DiscordColor.GREEN);
        } else if (type == Type.BADGE) {
            if (level == 1) { //prestige badges unlock at level 1
                user.getName().setBadge(name);
                if (user.getPrefs().get(Pref.NOTIFY_UNLOCK)) {
                    MessageUtils.sendMessage(user.asGuildMember().getPrivateChannel().block(), "Badge Unlocked!",
                            "You've unlocked the badge " + name + " for reaching " + getStatsRequired() + "!\n\nIt was equipped in your name automatically.",
                            DiscordColor.CYAN);
                }
            } else { //always gonna notify unlock for now
                MessageUtils.sendMessage(user.asGuildMember().getPrivateChannel().block(), "Badge Unlocked!",
                "You've unlocked the badge " + name + " for reaching " + getStatsRequired() + "!" +
                        "\n\nYou can type `/badge` on The Realm to equip it.",
                        DiscordColor.CYAN);
            }
        } else if (type == Type.NAME_COLOR && user.getPrefs().get(Pref.NOTIFY_UNLOCK)) {
            MessageUtils.sendMessage(user.asGuildMember().getPrivateChannel().block(), "Color Unlocked!",
                    "You've unlocked the name color **" + name + "** on The Realm!\n\n*You can type `/color list` on the server to view your unlocked colors.*",
                    BotUtils.getGuildRole(name, user.asGuildMember().getGuild().block()).getColor());
        }
    }

    public int getTotalLevelRequired() {
        return prestige * Progress.MAX_LEVEL + level;
    }

    public String getStatsRequired() {
        return (prestige > 0 ? "prestige " + prestige : "")
                + (level > 1 ? " level " + level : "");
    }

    @Override
    public String toString() {
        return name;
    }

}
