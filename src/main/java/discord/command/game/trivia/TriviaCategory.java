package discord.command.game.trivia;

public class TriviaCategory {

    private final String name;
    private final int id;

    TriviaCategory(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public int getID() {
        return id;
    }

}


