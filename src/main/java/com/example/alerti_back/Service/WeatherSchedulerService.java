package com.example.alerti_back.Service;

import com.example.alerti_back.Model.RainForecast;
import com.example.alerti_back.Model.Sensors;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WeatherSchedulerService {

    @Autowired
    private SupabaseService supabaseService;

    @Autowired
    private RainForecastService rainForecastService;

    @Autowired
    private WeatherService weatherService;

    private final WebClient webClient;

    @Value("${openweather.api.key}")
    private String apiKey;

    // Cache en mémoire pour les prévisions actuelles
    private final Map<String, RainForecast> currentForecasts = new ConcurrentHashMap<>();

    public WeatherSchedulerService(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("https://api.openweathermap.org").build();
    }

    /**
     * Au démarrage du backend, récupère immédiatement les prévisions pour tous les capteurs
     */
    @PostConstruct
    public void initializeForecasts() {
        System.out.println("🚀 Initialisation des prévisions météo au démarrage...");
        fetchForecastsForAllSensors();
    }

    /**
     * Programmation automatique : récupère les prévisions à chaque début de bloc (00h, 03h, 06h, 09h, 12h, 15h, 18h, 21h)
     */
    @Scheduled(cron = "0 0 0/3 * * ?")
    public void scheduledForecastUpdate() {
        System.out.println("⏰ Mise à jour programmée des prévisions météo - " + new Date());
        fetchForecastsForAllSensors();
    }

    private void fetchForecastsForAllSensors() {
        try {
            List<Sensors> sensors = supabaseService.getAllSensors();

            if (sensors.isEmpty()) {
                System.out.println("ℹ️ Aucun capteur trouvé dans la base de données");
                return;
            }

            System.out.println("📡 Récupération des prévisions pour " + sensors.size() + " capteur(s)");

            for (Sensors sensor : sensors) {
                if (sensor.getLatitude() != null && sensor.getLongitude() != null) {
                    fetchAndStoreForecastForSensor(sensor);
                } else {
                    System.out.println("⚠️ Capteur " + sensor.getId() + " sans coordonnées GPS, ignoré");
                }
            }

            System.out.println("✅ Mise à jour des prévisions terminée");

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des prévisions : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Récupère et stocke la prévision météo pour un capteur spécifique
     */
    private void fetchAndStoreForecastForSensor(Sensors sensor) {
        try {
            double lat = sensor.getLatitude();
            double lon = sensor.getLongitude();
            String sensorId = sensor.getId();

            System.out.println("🌤️ Récupération prévision pour capteur " + sensorId);

            // Appel API OpenWeather
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

            if (response == null || !response.get("cod").asText().equals("200")) {
                System.err.println("⚠️ Erreur API météo pour capteur " + sensorId);
                return;
            }

            JsonNode list = response.get("list");
            if (list != null && list.size() > 0) {

                // ✅ LOGIQUE SIMPLE : Prendre le prochain bloc disponible
                JsonNode selectedBlock = getNextAvailableBlock(list);

                if (selectedBlock != null) {
                    double rainAmount = 0.0;
                    JsonNode rainNode = selectedBlock.get("rain");
                    if (rainNode != null && rainNode.has("3h")) {
                        rainAmount = rainNode.get("3h").asDouble();
                    }

                    RainForecast forecast = new RainForecast();
                    forecast.setSensorId(sensorId);
                    forecast.setPluiePrevue(rainAmount);

                    long blockTimestamp = selectedBlock.get("dt").asLong();
                    Date blockStart = new Date(blockTimestamp * 1000);
                    Date blockEnd = new Date((blockTimestamp + (3 * 60 * 60)) * 1000);

                    forecast.setForecastFrom(blockStart);
                    forecast.setForecastTo(blockEnd);
                    forecast.setCreatedAt(new Date());

                    // Stocker en cache ET en base de données
                    currentForecasts.put(sensorId, forecast);
                    rainForecastService.saveRainForecast(forecast);

                    System.out.println("✅ Prévision " + sensorId + " : " + rainAmount + "mm (" +
                            formatDate(blockStart) + " - " + formatDate(blockEnd) + "), " );

                }
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur récupération prévision capteur " + sensor.getId() + " : " + e.getMessage());
        }
    }

    /**
     * ✅ NOUVELLE LOGIQUE SIMPLE : Récupère le prochain bloc disponible
     */
    private JsonNode getNextAvailableBlock(JsonNode forecastList) {
        long now = System.currentTimeMillis() / 1000;

        for (JsonNode forecast : forecastList) {
            long forecastTime = forecast.get("dt").asLong();

            // Prendre le premier bloc qui commence dans le futur
            if (forecastTime > now) {
                Date blockStart = new Date(forecastTime * 1000);
                Date blockEnd = new Date((forecastTime + 3 * 60 * 60) * 1000);

                System.out.println("🎯 Bloc sélectionné : " + formatDate(blockStart) + " - " + formatDate(blockEnd));
                return forecast;
            }
        }

        // Fallback : premier bloc disponible
        return forecastList.get(0);
    }

    /**
     * Récupère la prévision actuelle pour un capteur (depuis le cache)
     */
    public RainForecast getCurrentForecast(String sensorId) {
        RainForecast forecast = currentForecasts.get(sensorId);

        if (forecast != null) {
            Date now = new Date();

            // ✅ Vérifier si la prévision est encore valide
            if (now.before(forecast.getForecastTo())) {
                return forecast;
            } else {
                // Prévision expirée
                currentForecasts.remove(sensorId);
                System.out.println("⏰ Prévision expirée pour " + sensorId + ", mise à jour nécessaire");

            }
        }

        return null;
    }

    /**
     * ✅ Mise à jour d'un capteur spécifique si nécessaire
     */
    public void updateForecastIfNeeded(String sensorId) {
        RainForecast current = getCurrentForecast(sensorId);

        if (current == null) {
            try {
                List<Sensors> sensors = supabaseService.getAllSensors();
                for (Sensors sensor : sensors) {
                    if (sensor.getId().equals(sensorId) &&
                            sensor.getLatitude() != null && sensor.getLongitude() != null) {
                        fetchAndStoreForecastForSensor(sensor);
                        break;
                    }
                }
            } catch (Exception e) {
                System.err.println("❌ Erreur mise à jour capteur " + sensorId + " : " + e.getMessage());
            }
        }
    }

    /**
     * Méthode utilitaire pour formater les dates
     */
    private String formatDate(Date date) {
        return new java.text.SimpleDateFormat("dd/MM HH:mm").format(date);
    }

    /**
     * Forcer la mise à jour des prévisions
     */
    public void forceUpdate() {
        System.out.println("🔄 Mise à jour forcée des prévisions météo");
        fetchForecastsForAllSensors();
    }

    /**
     * Obtenir le statut du cache
     */
    public Map<String, Object> getCacheStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("totalCachedForecasts", currentForecasts.size());
        status.put("lastUpdate", new Date());

        List<Map<String, Object>> forecastDetails = new ArrayList<>();
        for (Map.Entry<String, RainForecast> entry : currentForecasts.entrySet()) {
            Map<String, Object> details = new HashMap<>();
            RainForecast forecast = entry.getValue();
            details.put("sensorId", entry.getKey());
            details.put("rainAmount", forecast.getPluiePrevue());
            details.put("validFrom", forecast.getForecastFrom());
            details.put("validUntil", forecast.getForecastTo());

            long timeLeft = forecast.getForecastTo().getTime() - new Date().getTime();
            long minutesLeft = Math.max(0, timeLeft / (1000 * 60));
            details.put("minutesLeft", minutesLeft);

            forecastDetails.add(details);
        }
        status.put("forecasts", forecastDetails);

        return status;
    }
}