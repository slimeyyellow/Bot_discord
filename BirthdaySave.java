package Event;


import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;


import java.io.FileWriter;
import java.io.IOException;

import static net.dv8tion.jda.api.interactions.commands.OptionType.*;

public class BirthdaySave extends ListenerAdapter {
    private JDA jda;

    public BirthdaySave(JDA jda) {
        this.jda = jda;
    }


    CommandListUpdateAction commands = jda.updateCommands();

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        super.onSlashCommandInteraction(event);

        commands.addCommands(Commands.slash("bday", "Add your birthday to the Server Calendar")
                .addOptions(new OptionData(USER, "user", "who's birthday?").setRequired(false))
                .addOptions(new OptionData(STRING, "Date", "Input your birthday in yyyy-MM-dd format").setRequired(true)))
                .queue();
        commands.addCommands((Commands.slash("Check_Age", "Check someone birthday and age since she or he were born")
                .addOption(new OptionData(USER, "user", "").setRequired(false))));

    }
    public void BirthdaySave(String Userid, String Date){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", USER);
        jsonObject.put("date", STRING);

        try (FileWriter file = new FileWriter("data.json")) {
            file.write(jsonObject.toString(4)); // Pretty-print JSON with indentation
            System.out.println("JSON file saved: " + jsonObject);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}