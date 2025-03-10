package Event;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import static net.dv8tion.jda.api.interactions.commands.OptionType.*;

public class BirthdaySave extends ListenerAdapter {
    private final JDA jda;

    // Constructor to initialize JDA
    public BirthdaySave(JDA jda) {
        this.jda = jda;
        registerCommands();
    }

    private void registerCommands() {
        jda.updateCommands().addCommands(
                Commands.slash("bday", "Add your birthday to the Server Calendar")
                        .addOptions(new OptionData(STRING, "date", "Format [yyyymmdd] Example : [19900515]").setRequired(true))
                        .addOptions(new OptionData(USER, "user", "Who's birthday?").setRequired(false))
        ).queue();
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("bday")) {
            String userId = event.getOption("user") != null
                    ? event.getOption("user").getAsUser().getId()
                    : event.getUser().getId();

            String date = event.getOption("date").getAsString();

            saveBirthday(userId, date);

            event.reply("I will remember for <@" + userId + ">Birthday on " + date).queue();
        }
    }

    public void saveBirthday(String userId, String date) {
        JSONArray birthdayArray;


        try (FileReader reader = new FileReader("data.json")) {
            Scanner scanner = new Scanner(reader);
            StringBuilder jsonString = new StringBuilder();
            while (scanner.hasNextLine()) {
                jsonString.append(scanner.nextLine());
            }
            scanner.close();
            birthdayArray = new JSONArray(jsonString.toString());
        } catch (IOException e) {
            birthdayArray = new JSONArray();
        }


        JSONObject newEntry = new JSONObject();
        newEntry.put("userId", userId);
        newEntry.put("date", date);

        for (int i = 0; i < birthdayArray.length(); i++) {
            JSONObject existingEntry = birthdayArray.getJSONObject(i);
            if (existingEntry.getString("userId").equals(userId)) {
                existingEntry.put("date", date);
                saveToFile(birthdayArray);
                return;
            }
        }

        birthdayArray.put(newEntry);
        saveToFile(birthdayArray);
    }

    private void saveToFile(JSONArray data) {
        try (FileWriter file = new FileWriter("data.json")) {
            file.write(data.toString(4)); // Pretty-print JSON with indentation
            System.out.println("🎉 JSON file updated: " + data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
