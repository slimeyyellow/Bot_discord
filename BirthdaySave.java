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

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;


import static net.dv8tion.jda.api.interactions.commands.OptionType.*;

public class BirthdaySave extends ListenerAdapter {
    private final JDA jda;

    // Constructor to initialize JDA
    public BirthdaySave(JDA jda) {
        this.jda = jda;
        registerCommands();
    }

    public void registerCommands() {
        jda.updateCommands().addCommands(
                Commands.slash("bday", "Add your birthday to the ser")
                        .addOptions(new OptionData(STRING, "date", "Input birthday in yyyy-MM-dd format").setRequired(true))
                        .addOptions(new OptionData(USER, "user", "Who's birthday?").setRequired(false)),

                Commands.slash("checkbday", "Check someone's birthday")
                        .addOptions(new OptionData(USER, "user", "Whose birthday do you want to check?").setRequired(true))
        ).queue();
    }


    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("bday")) {
            event.deferReply().queue();
            String userId = event.getOption("user") != null
                    ? event.getOption("user").getAsUser().getId(): event.getUser().getId();
            String date = event.getOption("date").getAsString();
            saveBirthday(userId, date);
            event.reply("I will remember for <@" + userId + ">Birthday on " + date).queue();
        }
        if (event.getName().equals("checkbday")){
            event.deferReply().queue();
            String userId = event.getOption("user").getAsUser().getId();
            String response = getBirthdayMessage(userId);
            event.getHook().sendMessage(response).queue();

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

    public String getBirthdayMessage(String userId) {
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
            return " No birthday data found. you can use /bday to add someone birthday";
        }


        for (int i = 0; i < birthdayArray.length(); i++) {
            JSONObject entry = birthdayArray.getJSONObject(i);
            if (entry.getString("userId").equals(userId)) {
                String birthdate = entry.getString("date");
                return formatBirthdayMessage(userId, birthdate);
            }
        }

        return "birthday not found for <@" + userId + ">.";
    }

    public String formatBirthdayMessage(String userId, String birthdate) {
        LocalDate birthDate = LocalDate.parse(birthdate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalDate today = LocalDate.now();

        // Calculate next birthday
        LocalDate nextBirthday = birthDate.withYear(today.getYear());
        if (nextBirthday.isBefore(today) || nextBirthday.isEqual(today)) {
            nextBirthday = nextBirthday.plusYears(1);
        }

        // Calculate time left & new age
        long monthsLeft = ChronoUnit.MONTHS.between(today.withDayOfMonth(1), nextBirthday.withDayOfMonth(1));
        int newAge = Period.between(birthDate, nextBirthday).getYears();

        // Convert to Discord Timestamp
        long unixTimestamp = nextBirthday.atStartOfDay(ZoneId.of("UTC")).toEpochSecond();
        String discordTime = "<t:" + unixTimestamp + ":D>";

        return "🎂 <@" + userId + ">'s next birthday will be on " + discordTime +
                ". They will be **" + newAge + "** years old. (" + monthsLeft + " months left!)";
    }



}
