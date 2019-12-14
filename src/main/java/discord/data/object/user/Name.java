package discord.data.object.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Name {

    @JsonIgnore
    private DUser user;

    private String nick;
    private String[] emojis;

    @JsonCreator
    protected Name(@JsonProperty("nick") String nick,
                   @JsonProperty("emojis") String[] emojis) {
        this.nick = nick;
        this.emojis = emojis;
    }

    protected Name(String nick) {
        this.nick = nick;
        this.emojis = new String[0];
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
        verifyOnGuild();
    }

    //used for jackson serialization
    public String[] getEmojis() {
        return this.emojis;
    }

    public void setEmojis(String[] emojis) {
        this.emojis = emojis;
        verifyOnGuild();
    }

    protected void setUser(DUser user) {
        this.user = user;
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
        if (emojis.length > 0) {
            sb.append(" ");
            for (String emoji : emojis) {
                sb.append(emoji);
            }
        }
        return sb.toString();
    }

}
