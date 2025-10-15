package com.example.alerti_back.Service;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Calendar;
import java.util.Date;

@Service
public class WeatherService {
    private final WebClient webClient;

    @Value("${openweather.api.key}")
    private String apiKey;

    // Injecte l'URL Supabase depuis application.properties
    @Value("${supabase.url}")
    private String supabaseUrl;

    // Injecte la clé d'API Supabase
    @Value("${supabase.key}")
    private String supabaseKey;
    // Utilisé pour faire des requêtes HTTP
    private final RestTemplate restTemplate = new RestTemplate();

    public WeatherService(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("https://api.openweathermap.org").build();
    }

    /**
     * Renvoie la pluie totale prévue sur les prochaines heures (somme des précipitations 3h).
     */
   /* public double getRainForecast(double lat, double lon) {
        try {
            JsonNode response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/data/2.5/forecast")
                            .queryParam("lat", lat)
                            .queryParam("lon", lon)
                            .queryParam("appid", apiKey)
                            .queryParam("units", "metric")
                            .build())
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            double totalRain = 0.0;
            JsonNode list = response.get("list");
            for (int i = 0; i < Math.min(8, list.size()); i++) {
                JsonNode rainNode = list.get(i).get("rain");
                if (rainNode != null && rainNode.has("3h")) {
                    totalRain += rainNode.get("3h").asDouble();
                }
            }
            return totalRain;
        } catch (Exception e) {
            System.err.println("❌ Erreur appel API météo : " + e.getMessage());
            return 0.0;
        }
    }*/
   /* public double getRainForecast(double lat, double lon) {
        try {
            JsonNode response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/data/2.5/forecast")
                            .queryParam("lat", lat)
                            .queryParam("lon", lon)
                            .queryParam("appid", apiKey)
                            .queryParam("units", "metric")
                            .build())
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            JsonNode list = response.get("list");
            if (list != null && list.size() > 0) {
                JsonNode rainNode = list.get(0).get("rain");
                if (rainNode != null && rainNode.has("3h")) {
                    return rainNode.get("3h").asDouble();  // ✅ Prévision pour les prochaines 3 heures uniquement
                }
            }
            return 0.0;
        } catch (Exception e) {
            System.err.println("❌ Erreur appel API météo : " + e.getMessage());
            return 0.0;
        }
    }*/

   /* public double getRainForecast(double lat, double lon) {
        try {
            // 🔁 Appel de l'API OpenWeatherMap pour récupérer les prévisions météo
            JsonNode response = webClient.get()

                    .uri(uriBuilder -> uriBuilder
                            .path("/data/2.5/forecast")               // Endpoint prévisions sur 5 jours par tranche de 3h
                            .queryParam("lat", lat)                   // Latitude du capteur
                            .queryParam("lon", lon)                   // Longitude du capteur
                            .queryParam("appid", apiKey)              // Clé API
                            .queryParam("units", "metric")            // Unités métriques (°C, mm)
                            .build())
                    .retrieve()
                    .bodyToMono(JsonNode.class)                       // Corps JSON de la réponse
                    .block();                                         // Requête synchrone

            // 📦 Accès à la liste de blocs de prévision (toutes les 3 heures)
            JsonNode list = response.get("list");
            if (list != null && list.size() > 0) {
                System.out.println("🌐 OpenWeather Response = " + response.toPrettyString());

                // 🕒 Définir la fenêtre de temps : de maintenant à maintenant + 3h
                long now = System.currentTimeMillis() / 1000;        // Timestamp actuel (en secondes)
                long threeHoursLater = now + (3 * 60 * 60);           // + 3 heures (en secondes)

                // 🔍 Parcourir les prévisions météo pour trouver celle dans les 3 prochaines heures
                for (JsonNode forecast : list) {
                    long forecastTime = forecast.get("dt").asLong(); // Timestamp du bloc météo

                    // ✅ Si le bloc météo est prévu dans les 3 prochaines heures
                    if (forecastTime >= now && forecastTime <= threeHoursLater) {
                        JsonNode rainNode = forecast.get("rain");    // Accès à la section "rain"

                        // 🌧️ Si la pluie est indiquée pour ce bloc, on la retourne
                        if (rainNode != null && rainNode.has("3h")) {
                            return rainNode.get("3h").asDouble();    // Pluie prévue (mm sur 3h)
                        } else {
                            return 0.0;                               // Pas de pluie prévue dans ce bloc
                        }
                    }
                }
            }

            // ❌ Aucun bloc trouvé dans la fenêtre de 3h, ou liste vide
            return 0.0;


        } catch (Exception e) {
            // 🧯 Gestion des erreurs (réseau, parsing JSON, etc.)
            System.err.println("❌ Erreur appel API météo : " + e.getMessage());

            return 0.0;
        }
    }*/
    public double getRainForecast(double lat, double lon) {
        try {
            System.out.println("🔍 Demande météo pour coordonnées: lat=" + lat + ", lon=" + lon);

            JsonNode response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/data/2.5/forecast")
                            .queryParam("lat", lat)
                            .queryParam("lon", lon)
                            .queryParam("appid", apiKey)
                            .queryParam("units", "metric")
                            .build())
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response == null) {
                System.err.println("⚠️ Réponse API météo vide");
                return 0.0;
            }

            if (response.has("cod") && !response.get("cod").asText().equals("200")) {
                System.err.println("⚠️ Erreur API météo: " + response.get("message").asText());
                return 0.0;
            }

            JsonNode list = response.get("list");
            if (list != null && list.size() > 0) {
                long now = System.currentTimeMillis() / 1000;
                long threeHoursLater = now + (3 * 60 * 60);

                for (JsonNode forecast : list) {
                    long forecastTime = forecast.get("dt").asLong();

                    if (forecastTime >= now && forecastTime <= threeHoursLater) {
                        JsonNode rainNode = forecast.get("rain");
                        if (rainNode != null && rainNode.has("3h")) {
                            double pluie = rainNode.get("3h").asDouble();

                            Date startDate = new Date(forecastTime * 1000);
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(startDate);
                            cal.add(Calendar.HOUR_OF_DAY, 3);
                            Date endDate = cal.getTime();

                            System.out.println("📆 Bloc utilisé : " + formatDate(startDate) + " - " + formatDate(endDate));
                            System.out.println("🌧️ Pluie prévue dans ce bloc : " + pluie + " mm");

                            return pluie;
                        } else {
                            System.out.println("🌤️ Pas de pluie prévue dans ce bloc.");
                        }
                    }
                }
            }

            System.out.println("ℹ️ Aucun bloc avec prévision de pluie trouvé dans les 3 prochaines heures.");
            return 0.0;

        } catch (Exception e) {
            System.err.println("❌ Erreur appel API météo : " + e.getMessage());
            e.printStackTrace();
            return 0.0;
        }
    }

    // 💡 Méthode utilitaire pour formater les dates
    private String formatDate(Date date) {
        return new java.text.SimpleDateFormat("HH:mm").format(date);
    }




    // Ajoutez cette méthode dans WeatherService et appelez-la au démarrage
    @PostConstruct
    public void testOpenWeatherAPI() {
        try {
            System.out.println("🧪 Test de l'API OpenWeather...");
            System.out.println("🔑 Clé API: " + apiKey);

            // Coordonnées de test (Paris)
            double testLat = 48.8566;
            double testLon = 2.3522;

            JsonNode response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/data/2.5/forecast")
                            .queryParam("lat", testLat)
                            .queryParam("lon", testLon)
                            .queryParam("appid", apiKey)
                            .queryParam("units", "metric")
                            .build())
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response != null) {
                System.out.println("✅ Test API OpenWeather réussi!");
                if (response.has("cod")) {
                    System.out.println("📋 Code de réponse: " + response.get("cod").asText());
                }
            } else {
                System.out.println("❌ Test API OpenWeather: réponse nulle");
            }
        } catch (Exception e) {
            System.err.println("❌ Test API OpenWeather échoué: " + e.getMessage());
            e.printStackTrace();
        }
    }


}
