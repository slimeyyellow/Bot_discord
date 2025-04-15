//-------------------- CREDITS-------------------------------
//Jisei`ichi's APCS Principal project
//OpenAI GPT 4.o helping Fix error and clearing part of code
package Event;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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
                        .addOptions(new OptionData(STRING, "date", "Input birthday in yyyy-MM-dd format : 2008-08-30").setRequired(true))
                        .addOptions(new OptionData(USER, "user", "Who's birthday?").setRequired(false)),
                Commands.slash("checkbday", "Check someone's birthday")
                        .addOptions(new OptionData(USER, "user", "Whose birthday do you want to check?").setRequired(true)),
                Commands.slash("editbday","Edit birthday")
                        .addOptions(new OptionData(STRING,"date","edit birthday date"))
                        .addOptions(new OptionData(USER,"user","who's birthday? ")),
                Commands.slash("forgotbday","remove birthday from data")
                        .addOptions(new OptionData(USER,"user","who's birthday?").setRequired(true)),
                Commands.slash("nextbirthday", "Show list from the soonest to the fartest birthday")
        ).queue();
    }


    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("bday")) {
            String userId = event.getOption("user") != null
                    ? event.getOption("user").getAsUser().getId(): event.getUser().getId();
            String date = event.getOption("date").getAsString();
            if (birthdayExists(userId)) {
                event.reply("The data already exists. If you wish to edit the birthday, use /editbday").queue();
            } else {
                saveBirthday(userId, date);
                event.reply("I will remember <@" + userId + ">'s Birthday on " + date).queue();
            }
        }
        if (event.getName().equals("checkbday")){
            event.deferReply().queue();
            String userId = event.getOption("user").getAsUser().getId();
            String response = getBirthdayMessage(userId);
            event.getHook().sendMessage(response).queue();

        }
        if (event.getName().equals("editbday")){
            String userId = event.getOption("user") != null
                    ? event.getOption("user").getAsUser().getId(): event.getUser().getId();
            String date = event.getOption("date").getAsString();
            if (birthdayExists(userId)){
            saveBirthday(userId, date);
            event.reply("birthday updated for <@"+userId+">'s birthday on "+date).queue();
            } else {
                event.reply("birthday not found. use `/bday` to add your birthday").queue();
            }
        }
        if (event.getName().equals("forgotbday")){
            event.deferReply().queue();
            String userId = event.getOption("user").getAsUser().getId();
            String response = deleteBirthday(userId);
            event.getHook().sendMessage(response).queue();

        }
        if (event.getName().equals("nextbirthday")){
            event.deferReply().queue();
            String nextbirthday = getUpcomingBirthdays();
            event.replyEmbeds(getUpcomingBirthdays()).queue();
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
        try {
            // Parse the birthday date
            LocalDate birthDate = LocalDate.parse(birthdate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            LocalDate today = LocalDate.now();

            // Calculate next birthday
            LocalDate nextBirthday = birthDate.withYear(today.getYear());
            if (nextBirthday.isBefore(today) || nextBirthday.isEqual(today)) {
                nextBirthday = nextBirthday.plusYears(1);
            }

            long monthsLeft = ChronoUnit.MONTHS.between(today.withDayOfMonth(1), nextBirthday.withDayOfMonth(1));
            int newAge = Period.between(birthDate, nextBirthday).getYears();
            long unixTimestamp = nextBirthday.atStartOfDay(ZoneId.of("UTC")).toEpochSecond();
            String discordTime = "<t:" + unixTimestamp + ":D>";

            return "<@" + userId + ">'s next birthday will be on " + discordTime +
                    ". They will be **" + newAge + "** years old. (" + monthsLeft + " months left!)";}
        catch (Exception e) {
            e.printStackTrace();
            return "⚠️ Error processing birthday date. Make sure it's in the format `yyyy-MM-dd`.";
        }
    }

    public boolean birthdayExists(String userId) {
        try (FileReader reader = new FileReader("data.json")) {
            Scanner scanner = new Scanner(reader);
            StringBuilder jsonString = new StringBuilder();
            while (scanner.hasNextLine()) {
                jsonString.append(scanner.nextLine());
            }
            scanner.close();
            JSONArray birthdayArray = new JSONArray(jsonString.toString());
            for (int i = 0; i < birthdayArray.length(); i++) {
                JSONObject entry = birthdayArray.getJSONObject(i);
                if (entry.getString("userId").equals(userId)) {return true;}
            }
        } catch (IOException e) {
            System.err.println("Error reading data.json: " + e.getMessage());
        }return false;
    }

    public String deleteBirthday(String userId) {
        JSONArray birthdayArray;

        try (FileReader reader = new FileReader("data.json")) {
            Scanner scanner = new Scanner(reader);
            StringBuilder jsonString = new StringBuilder();

            while (scanner.hasNextLine()) {
                jsonString.append(scanner.nextLine());
            }
            scanner.close();

            birthdayArray = new JSONArray(jsonString.toString());
            JSONArray updatedArray = new JSONArray();

            boolean found = false;
            for (int i = 0; i < birthdayArray.length(); i++) {
                JSONObject entry = birthdayArray.getJSONObject(i);
                if (!entry.getString("userId").equals(userId)) {
                    updatedArray.put(entry); // Keep all other birthdays
                } else {
                    found = true;
                }
            }

            if (!found) {
                return "No birthday found for <@" + userId + ">. Use `/bday` to input your birthday.";
            }

            // Save updated JSON back to file
            try (FileWriter file = new FileWriter("data.json")) {
                file.write(updatedArray.toString(4)); // Pretty-print JSON
            }

            return "Birthday successfully forgotten for <@" + userId + ">.";

        } catch (IOException e) {
            e.printStackTrace();
            return "⚠️ An error occurred while deleting the birthday.";
        }
    }

    public String getUpcomingBirthdays(){
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
            return new EmbedBuilder()
                    .setTitle("📅 Upcoming Birthdays")
                    .setDescription("⚠️ No birthday data found.")
                    .setColor(Color.RED)
                    .build();
        }

        List<JSONObject> sortedList = new ArrayList<>();
        for (int i = 0; i < birthdayArray.length(); i++) {
            sortedList.add(birthdayArray.getJSONObject(i));
        }

        LocalDate today = LocalDate.now();

        sortedList.sort(Comparator.comparing(o -> {
            LocalDate bday = LocalDate.parse(o.getString("date"));
            LocalDate nextBday = bday.withYear(today.getYear());
            if (!nextBday.isAfter(today)) {
                nextBday = nextBday.plusYears(1);
            }
            return nextBday;
        }));

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("📅 Upcoming Birthdays");
        embed.setColor(Color.MAGENTA);

        for (JSONObject entry : sortedList) {
            String userId = entry.getString("userId");
            String dateStr = entry.getString("date");
            LocalDate birthDate = LocalDate.parse(dateStr);
            LocalDate nextBirthday = birthDate.withYear(today.getYear());

            if (!nextBirthday.isAfter(today)) {
                nextBirthday = nextBirthday.plusYears(1);
            }

            long unixTime = nextBirthday.atStartOfDay(ZoneId.of("UTC")).toEpochSecond();
            int age = Period.between(birthDate, nextBirthday).getYears();
            String emoji = getAgeEmoji(age);

            String fieldValue = emoji + " Turns **" + age + "** on <t:" + unixTime + ":D> (`" + dateStr + "`) – <t:" + unixTime + ":R>";
            embed.addField("🎉 <@" + userId + ">", fieldValue, false);
        }

        return embed.build();
}

    private String getAgeEmoji(int age) {
        if (age <= 12) return "🧒";
        if (age <= 19) return "🧑‍🎓";
        if (age <= 29) return "🧑";
        if (age <= 49) return "🧑‍💼";
        if (age <= 64) return "👴";
        if (age <=200) return"👵";
        return "👻";
    }



}

