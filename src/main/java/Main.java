import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDate;
import java.util.Scanner;

public class Main {

    public static JsonNode fetchMenu(String location, String date) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.hfs.purdue.edu/menus/v2/locations/" + location + "/" + date))
                .GET()
                .build();
        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200){
            throw new IOException("Status code error: " + response.statusCode());
        }
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.body());
        return root;
    }

    public static int[] analyzeMenu(JsonNode root, boolean vegOnly, String selectedMeal, String preference) {
        JsonNode meals = root.get("Meals");
        int matchingCount = 0;
        int eligibleCount = 0;
        for(JsonNode mealType : meals) {
            String mealName = mealType.get("Name").asText();
            if (!mealName.equalsIgnoreCase(selectedMeal)) {
                continue;
            }
            JsonNode station = mealType.get("Stations");
            for(JsonNode stationType : station) {
                JsonNode foods = stationType.get("Items");
                for(JsonNode food : foods) {
                    String foodName = food.get("Name").asText();
                    boolean isVeg = food.get("IsVegetarian").asBoolean();
                    boolean nutrition = food.get("NutritionReady").asBoolean();
                    if(!vegOnly || (isVeg && nutrition)) {
                        if(foodName.toLowerCase().contains(preference) || preference.isEmpty()) {
                            matchingCount++;   
                        }
                        eligibleCount++;
                    }
                }
            }
           
        }
        return new int[]{matchingCount, eligibleCount};
    }

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        LocalDate today = LocalDate.now();
        String formattedDate = today.toString();

        System.out.println("========================================");
        System.out.println("              BOILERBITES");
        System.out.println("       Purdue Dining Recommendation");
        System.out.println("========================================");
        System.out.println();
        System.out.println("Date: " + formattedDate);
        System.out.println();

        
        String vegOption;
        do {
            System.out.print("Are you vegetarian? (yes/no): ");
            vegOption = input.nextLine();
            vegOption = vegOption.trim().toLowerCase();

            if(!vegOption.equals("yes") && !vegOption.equals("no")) {
                System.out.println("Invalid option. Try again");
            }

        } while (!vegOption.equals("yes") && !vegOption.equals("no"));

        boolean vegPreference = false;
        if (vegOption.equals("yes")) {
            vegPreference = true;
        }



        String mealChoice;
        do {
            System.out.print("Which meal are you looking for?\n1. Breakfast\n2. Lunch\n3. Dinner\nEnter your choice: ");
            mealChoice = input.nextLine();
            mealChoice = mealChoice.trim();

            if(!mealChoice.equals("1") && !mealChoice.equals("2") && !mealChoice.equals("3")) {
                System.out.println("Invalid option. Try again");
            }

        } while (!mealChoice.equals("1") && !mealChoice.equals("2") && !mealChoice.equals("3"));

        if(mealChoice.equals("1")) {
            mealChoice = "Breakfast";
        } else if (mealChoice.equals("2")) {
            mealChoice = "Lunch";
        } else {
            mealChoice = "Dinner";
        }

        System.out.print("What food are you craving?\n(Press enter if you have no preference): ");
        String preference = input.nextLine();
        preference = preference.trim().toLowerCase();

        
        String[] diningHallLocs = {"Wiley", "Ford", "Earhart", "Windsor", "Hillenbrand"};

        System.out.println();
        System.out.println("------------ YOUR PREFERENCES ----------");
        System.out.printf("%-18s %s%n", "Vegetarian Only:", vegPreference);
        System.out.printf("%-18s %s%n", "Selected Meal:", mealChoice);
        System.out.printf("%-18s %s%n", "Food Preference:", preference);
        System.out.println("----------------------------------------");
        System.out.println();
        System.out.println("Analyzing dining halls...");


        String bestHall = "";
        String altHall = "";
        int altEligibleCount = 0;
        int bestMatchCount = 0;
        int menusAccessed = 0;
        int bestEligibleCount = 0;

        for(int i = 0; i < diningHallLocs.length; i++) {
            try{
                JsonNode temp = fetchMenu(diningHallLocs[i], formattedDate);
                int[] menuCount = analyzeMenu(temp, vegPreference, mealChoice, preference);
                menusAccessed++;
                if(menuCount[0] > bestMatchCount) {
                    bestMatchCount = menuCount[0];
                    bestHall = diningHallLocs[i];
                    bestEligibleCount = menuCount[1];
                }
                if(menuCount[1] > altEligibleCount) {
                    altHall = diningHallLocs[i];
                    altEligibleCount = menuCount[1];
                }
                System.out.printf("%-12s %3d eligible | %3d preferred%n", diningHallLocs[i], menuCount[1], menuCount[0]);
            } catch (IOException e) {
                System.out.println(diningHallLocs[i] + ": Unable to retrieve menu. " + e.getMessage());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Menu retrieval interrupted.");
                return;
            }
            
        }
        if(menusAccessed == 0) {
            System.out.println("Unable to retrieve menu data.");
        } else if (altEligibleCount == 0) {
            System.out.println("No dining halls with matching/eligible food items were found.");
        } else if (bestMatchCount == 0) {
            System.out.println();
            System.out.println("========================================");
            System.out.println("          NO PREFERRED MATCHES");
            System.out.println("========================================");
            System.out.println();

            System.out.printf("No food items matching \"%s\" were found%n", preference);
            System.out.println("for your selected meal and dietary preference.");
            System.out.println();

            System.out.println("However, other eligible food options are available.");
            System.out.println();

            System.out.printf("Alternative Dining Hall: %s%n", altHall);
            System.out.printf("Total Eligible Options: %d%n", altEligibleCount);
            System.out.println();

            System.out.println("Reason:");
            System.out.printf("%s offers the most eligible food options%n", altHall);
            System.out.println("for your selected meal and dietary preference.");
            System.out.println();

            System.out.println("Try another keyword or explore these alternatives.");
        } else {
            System.out.println();
            System.out.println("========================================");
            System.out.println("          YOUR RECOMMENDATION");
            System.out.println("========================================");
            System.out.println();

            System.out.printf("Recommended Dining Hall: %s%n", bestHall);
            System.out.println();
            System.out.println("Reason: ");
            if(preference.equals("")) {
                System.out.printf("%s has the most eligible food options%n", bestHall);
                System.out.println("for your selected meal and dietary preference (since no preference was set).");
            } else {
                System.out.printf("%s has the most matching \"%s\" options%n", bestHall, preference);
                System.out.println("for your selected meal and dietary preference.");
            }
            System.out.println();
            System.out.printf("Preferred Food Matches: %d%n", bestMatchCount);
            System.out.printf("Total Eligible Options: %d%n", bestEligibleCount);
            System.out.println();
            System.out.println("Enjoy your meal!");
        }
        
        System.out.println("========================================");
        input.close();
    }
}