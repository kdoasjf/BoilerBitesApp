import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

public class Main {
    public static void main(String[] args) {
        System.out.println("Welcome to BoilerBites!");
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.hfs.purdue.edu/menus/v2/locations/Wiley/2026-11-23"))
                    .GET()
                    .build();
            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println(response.statusCode());
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.body());

            JsonNode meals = root.get("Meals");
            String diningHallName = root.get("Location").asText();
            String date = root.get("Date").asText();

            System.out.println("Dining Hall: " + diningHallName);
            System.out.println("Date: " + date);
            System.out.println("Number of meals: " + meals.size());
            int totalCount = 0;
            for(JsonNode mealType : meals) {
                System.out.println(mealType.get("Name").asText());
                JsonNode station = mealType.get("Stations");
                for(JsonNode stationType : station) {
                    System.out.println("\t" + stationType.get("Name").asText());
                    JsonNode foods = stationType.get("Items");
                    for(JsonNode food : foods) {
                        totalCount++;
                        String foodName = food.get("Name").asText();
                        boolean isVeg = food.get("IsVegetarian").asBoolean();
                        boolean nutrition = food.get("NutritionReady").asBoolean();
                        System.out.println("\t\t" + foodName);
                        System.out.println("\t\tVegetarian: " + isVeg);
                        System.out.println("\t\tNutrition Ready: " + nutrition + "\n");
                    }
                }
            }
            System.out.println("Total food items: " + totalCount);
        } catch (IOException | InterruptedException exception) {
            System.err.println("Unable to retrieve the Purdue dining menu: " + exception.getMessage());
        }
        
    }
}