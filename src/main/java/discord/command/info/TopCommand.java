package discord.command.info;

import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.data.UserManager;
import discord.data.object.user.User;
import discord.util.BotUtils;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

public class TopCommand extends AbstractCommand {
    
    public TopCommand() {
        super(new String[] {"top", "leaderboard", "rankings"}, 0, CommandCategory.INFO);
    }
    
    @Override
    public void execute(IMessage message, String[] args) {
        if (args.length > 0 && args[0].matches("\\D+")) {
            BotUtils.sendErrorMessage(message.getChannel(), "Couldn't parse a valid amount of users to display.");
            return;
        }
        
        int amount = 10;       
        if (args.length > 0) {
            amount = Integer.parseInt(args[0]);
            if (amount > 25) {
                amount = 25;
            } else if (amount < 1) {
                BotUtils.sendErrorMessage(message.getChannel(), 
                        "Please specify a valid amount of users (1-25) to display.");
                return;             
            }
        }
      
        List<User> users = new ArrayList<>(UserManager.getUsers());
        Collections.sort(users, (User user1, User user2) -> 
                user1.getProgress().getTotalXP() - user2.getProgress().getTotalXP());
        Collections.reverse(users);
        String desc = "";
        for (int i = 0; i < amount; i++) {
            User user = users.get(i);
            desc += String.format("**%d)** %s `Lvl `**`%,d`**`   -   `**`%,d`**` XP`\n", 
                    i + 1, 
                    BotUtils.getMention(user), 
                    user.getProgress().getTotalLevel(), 
                    user.getProgress().getTotalXP());
        }
        
        int totalXP = 0;
        for (User user : users) {
            totalXP += user.getProgress().getTotalXP();
        }
        
        EmbedBuilder builder = BotUtils.getBuilder(message.getClient(), "Top " + amount + " Users", desc);
        builder.withFooterText(String.format("%,d", totalXP) + " XP has been earned on this guild.");
        builder.withColor(Color.CYAN);
                                 
        BotUtils.sendEmbedMessage(message.getChannel(), builder.build());
    }
    
    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "", "View the top users progressed on this guild."
                + "\n\nOptionally, you can specify a number (1-25) of users to display as an argument.");
    }
       
}
