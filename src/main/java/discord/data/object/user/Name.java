package discord.data.object.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

public class Name {

    @JsonIgnore
    private DUser user;

    private String nick;
    private int emojiCP;

    @JsonCreator
    protected Name(@JsonProperty("nick") String nick,
                   @JsonProperty("emojiCP") int emojiCP) {
        this.nick = nick;
        this.emojiCP = emojiCP;
    }

    protected Name(String nick) {
        this.nick = nick;
        this.emojiCP = 0;
    }

    public String getNick() {
        return nick;
    }

    protected void setUser(DUser user) {
        this.user = user;
    }

    public void setNick(String nick) {
        this.nick = nick;
        verifyOnGuild();
    }

    public void setEmoji(int codepoint) {
        this.emojiCP = codepoint;
        verifyOnGuild();
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

        user.asGuildMember().edit(spec -> spec.setNickname(name)).block();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Prestige prestige = user.getProg().getPrestige();
        if (prestige.getNumber() > 0) {
            sb.append(prestige.getBadge()).append(" ");
        }
        sb.append(nick);
        if (emojiCP > 0) {
            sb.append(" ").appendCodePoint(emojiCP);
        }
        return sb.toString();
    }

}
