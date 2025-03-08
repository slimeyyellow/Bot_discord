package Event;

import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

public class HewwoEvent extends ListenerAdapter {
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String[] messagesent = event.getMessage().getContentRaw().split(" ");
        for (String word : messagesent) {
            if (word.equalsIgnoreCase("hello") || word.equalsIgnoreCase("helo")) {
                event.getChannel().sendMessage("HI!").queue();
                break;
            }
        }
    }
}
