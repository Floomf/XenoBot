package discord.data.object.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

//Immutable
public class Reincarnation {

    private static final int MAX_REINCARNATION = 20;

    private static final String[][] REINCARNATIONS = {
        {"", ""},
        {"安心", "Anshin"},
        {"挽回", "Bankai"},
        {"超越", "Chouetsu"},
        {"道理", "Douri"},
        {"永遠", "Eien"},
        {"腐敗", "Fuhai"},
        {"下痢", "Geri"},
        {"繁盛", "Hanjou"},
        {"意味", "Imi"},
        {"慈悲", "Jihi"},
        {"苦痛", "Kutsū"},
        {"無罪", "Muzai"},
        {"忍耐", "Nintai"},
        {"恐れ", "Osore"},
        {"恋愛", "Renai"},
        {"真実", "Shinjitsu"},
        {"統一", "Touitsu"},
        {"有無", "Umu"},
        {"欲望", "Yokubou"},
        {"全滅", "Zenmetsu"}
    };

    private final int number;

    @JsonCreator
    public Reincarnation(@JsonProperty("number") int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    @JsonIgnore
    public String getKanji() {
        return REINCARNATIONS[number][0];
    }

    @JsonIgnore
    public String getRomaji() {
        return REINCARNATIONS[number][1];
    }

    @JsonIgnore
    public boolean isReincarnated() {
        return number > 0;
    }

    @JsonIgnore
    public boolean isMax() {
        return number == MAX_REINCARNATION;
    }

    protected Reincarnation reincarnate() {
        return new Reincarnation(number + 1);
    }

}
