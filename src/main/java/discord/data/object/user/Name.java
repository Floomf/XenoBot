package discord.data.object.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import discord4j.core.spec.GuildMemberEditSpec;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Name {

    @JsonIgnore
    private DUser user;

    private String badge;
    private String nick;
    private String emojis;

    @JsonCreator
    protected Name(@JsonProperty("badge") String badge,
                   @JsonProperty("nick") String nick,
                   @JsonProperty("emojis") String emojis) {
        this.badge = badge;
        this.nick = nick;
        this.emojis = emojis;
    }

    protected Name(String nick) {
        this.badge = "";
        this.nick = nick;
        this.emojis = "";
    }

    public String getBadge() {
        return badge;
    }

    public String getNick() {
        return nick;
    }

    public String getEmojis() {
        return emojis;
    }

    //no check in place to see if its an actual badge, possibly pass an enum?
    public void setBadge(String badge) {
        this.badge = badge;
        verifyOnGuild();
    }

    public void setNick(String nick) {
        this.nick = nick;
        if (isOverflowed()) {
            if (nick.length() >= 29) {
                emojis = "";
            } else {
                emojis = emojis.substring(0, 29 - nick.length());
            }
        }
        verifyOnGuild();
    }

    public void setEmojis(String emojisToSet) {
        emojis = emojisToSet;

        if (isOverflowed()) {
            nick = nick.substring(0, 29 - emojis.length());
        }
        verifyOnGuild();
    }

    protected void setUser(DUser user) {
        this.user = user;
    }

    @JsonIgnore
    private boolean isOverflowed() {
        return !emojis.isEmpty() && nick.length() + emojis.length() > 29;
    }

    public void verifyOnGuild() {
        String name = this.toString();
        System.out.println("Verifying name " + name + " for " + this.getNick());
        Optional<String> discordNick = user.asGuildMember().getNickname();
        if (discordNick.isPresent() && discordNick.get().equals(name)) {
            return;
        } else if (!discordNick.isPresent() && user.asGuildMember().getDisplayName().equals(name)) {
            return;
        }

        if (user.asGuildMember().equals(user.asGuildMember().getGuild().block().getOwner().block())) {
            System.out.println("Need to set owner's name to " + name);
            return;
        }

        System.out.println("Set nick " + name + " for " + this.getNick());

        user.asGuildMember().edit(GuildMemberEditSpec.create().withNicknameOrNull(name)).block();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (!badge.isEmpty()) {
            sb.append(badge).append(" ");
        }
        sb.append(nick);
        if (emojis.length() > 0) {
            sb.append(" ");
            sb.append(emojis);
        }
        return sb.toString();
    }

}
