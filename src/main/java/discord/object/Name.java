package discord.object;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import discord.BotUtils;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

public class Name {
    
    @JsonIgnore
    private User user;
    
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
    
    public int getEmojiCP() {
        return emojiCP;
    }
    
    protected void setUser(User user) {
        this.user = user;
    }
    
    public void setNick(String nick, IGuild guild) {
        this.nick = nick;
        verify(guild);
    }
    
    public void setEmoji(int codepoint, IGuild guild) {
        this.emojiCP = codepoint;
        verify(guild);
    }
    
    public void verify(IGuild guild) {
        String name = this.toString();
        IUser dUser = guild.getUserByID(user.getDiscordID());
        String dNick = dUser.getNicknameForGuild(guild);               
        if ((dNick == null && !dUser.getName().equals(name)) 
                || (dNick != null && !dNick.equals(name))) {
            if (guild.getOwner().equals(dUser)) {
                System.out.println("Need to set owner's name to " + name);
                return;
            }
            BotUtils.setUserNickname(guild, dUser, name);
        }
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Prestige prestige = user.getProgress().getPrestige();
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
