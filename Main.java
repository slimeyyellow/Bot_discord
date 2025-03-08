import Event.BirthdaySave;
import Event.HewwoEvent;

import Event.TellJoke;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

import java.util.EnumSet;

public class Main {
    public static void main(String args[]) throws Exception{
        JDABuilder builder = JDABuilder.createDefault(
                "Token");
        builder.enableIntents(GatewayIntent.MESSAGE_CONTENT).setActivity(Activity.watching("You"));
        JDA jda = builder.build().awaitReady();
        jda.updateCommands().queue();

        CommandListUpdateAction commands = jda.updateCommands();
        jda.awaitReady();
        jda.addEventListener(new HewwoEvent());
        jda.addEventListener(new BirthdaySave(jda));
        jda.addEventListener(new TellJoke());
        Message.suppressContentIntentWarning();



    }
}
