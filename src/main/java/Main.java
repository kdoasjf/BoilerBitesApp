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

    public static int analyzeMenu(JsonNode root, boolean vegOnly, String selectedMeal) {
        JsonNode meals = root.get("Meals");
        int matchingCount = 0;
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
                        matchingCount++;
                    }
                }
            }
           
        }
        return matchingCount;
    }

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        LocalDate today = LocalDate.now();
        String formattedDate = today.toString();

        System.out.println("Welcome to BoilerBites!");
        System.out.println("Date: " + formattedDate);

        
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

        
        String[] diningHallLocs = {"Wiley", "Ford", "Earhart", "Windsor", "Hillenbrand"};

        
        System.out.println("Vegetarian Only: " + vegPreference);
        System.out.println("Selected Meal: " + mealChoice);
        System.out.println();

        String bestHall = "";
        int bestMatchCount = 0;
        int menusAccessed = 0;

        for(int i = 0; i < diningHallLocs.length; i++) {
            try{
                JsonNode temp = fetchMenu(diningHallLocs[i], formattedDate);
                int menuCount = analyzeMenu(temp, vegPreference, mealChoice);
                menusAccessed++;
                if(menuCount > bestMatchCount ) {
                    bestMatchCount = menuCount;
                    bestHall = diningHallLocs[i];
                }
                System.out.println(diningHallLocs[i] + ": " + menuCount + " matching items");
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
        } else if (bestMatchCount == 0) {
            System.out.println("No dining halls with matching food items were found.");
        } else{
            System.out.println("Recommended Dining Hall: " + bestHall);
            System.out.println("Matching Food Items: " + bestMatchCount);
        }
        input.close();
    }
}