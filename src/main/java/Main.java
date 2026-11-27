import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

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

    public static int analyzeMenu(JsonNode root, boolean vegOnly) {
        JsonNode meals = root.get("Meals");
        int matchingCount = 0;
        for(JsonNode mealType : meals) {
            //System.out.println(mealType.get("Name").asText());
            JsonNode station = mealType.get("Stations");
            for(JsonNode stationType : station) {
                //System.out.println("\t" + stationType.get("Name").asText());
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
        System.out.println("Welcome to BoilerBites!");
        try {
            JsonNode root = fetchMenu("Wiley", "2026-11-23");
            
            String diningHallName = root.get("Location").asText();
            String date = root.get("Date").asText();

            int menuCount = analyzeMenu(root, true);
            System.out.println("Dining Hall: " + diningHallName);
            System.out.println("Date: " + date);
            System.out.println("Matching food items: " + menuCount);
        } catch (IOException | InterruptedException exception) {
            System.err.println("Unable to retrieve the Purdue dining menu: " + exception.getMessage());
        }
        
    }
}