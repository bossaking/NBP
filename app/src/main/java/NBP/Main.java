/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package NBP;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.*;

/**
 *
 * @author Comarch
 */
public class Main {
        public static void main(String[] args) {
        //MainApp.main(args);
        
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("http://api.nbp.pl/api/exchangerates/tables/A/2021-10-25")).build();
        try{
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JSONArray array = new JSONArray(response.body());
        JSONObject obj = array.getJSONObject(0);
        JSONArray rates = obj.getJSONArray("rates");
        System.out.println(rates);
        }catch(Exception e){
            System.out.println(e.getMessage());
        }

    }
    
}
