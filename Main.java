import Event.BirthdaySave;
import Event.HewwoEvent;

import Event.TellJoke;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.internal.utils.JDALogger;

import java.util.EnumSet;

public class Main {
    public static void main(String args[]) throws Exception{
        JDABuilder builder = JDABuilder.createDefault(
                "BOT TOKEN")
                .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS);
        builder.enableIntents(GatewayIntent.MESSAGE_CONTENT).setActivity(Activity.watching("You"));
        JDA jda = builder.build().awaitReady();
        jda.updateCommands().queue();
        JDALogger.setFallbackLoggerEnabled(false);

        CommandListUpdateAction commands = jda.updateCommands();
        jda.awaitReady();
        jda.addEventListener(new HewwoEvent());
        jda.addEventListener(new BirthdaySave(jda));
        jda.addEventListener(new TellJoke());
        Message.suppressContentIntentWarning();



    }
}
