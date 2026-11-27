import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.IOException;

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
            System.out.println(response.body());
        } catch (IOException | InterruptedException exception) {
            System.err.println("Unable to retrieve the Purdue dining menu: " + exception.getMessage());
        }
    }
}