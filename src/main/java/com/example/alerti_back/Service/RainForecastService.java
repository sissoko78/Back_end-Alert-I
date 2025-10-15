package com.example.alerti_back.Service;

import com.example.alerti_back.Model.RainForecast;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference;


import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class RainForecastService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public void saveRainForecast(RainForecast forecast) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", supabaseKey);
        headers.set("Authorization", "Bearer " + supabaseKey);
        headers.set("Prefer", "return=representation");
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> payload = new HashMap<>();
        payload.put("sensor_id", forecast.getSensorId());
        payload.put("pluie_prevue", forecast.getPluiePrevue());
        payload.put("forecast_from", toISO(forecast.getForecastFrom()));
        payload.put("forecast_to", toISO(forecast.getForecastTo()));
        payload.put("created_at", toISO(forecast.getCreatedAt()));
        payload.put("forecast_block", forecast.getForecastBlock()); // Ajout du bloc

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                supabaseUrl + "/rest/v1/rain_forecast",
                HttpMethod.POST,
                request,
                String.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            System.err.println("❌ Échec de l'insertion dans rain_forecast : " + response.getBody());
        }
    }

    private String toISO(Date date) {
        SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        iso.setTimeZone(TimeZone.getTimeZone("UTC"));
        return iso.format(date);
    }

    // ✅ 1. Récupérer toutes les prévisions d’un capteur donné
    public List<RainForecast> getForecastsBySensorId(String sensorId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", supabaseKey);
        headers.set("Authorization", "Bearer " + supabaseKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = supabaseUrl + "/rest/v1/rain_forecast?sensor_id=eq." + sensorId + "&order=forecast_from.desc";

        ResponseEntity<List<RainForecast>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<RainForecast>>() {}
        );

        return response.getBody();
    }


    // ✅ 2. Récupérer les prévisions d’un capteur dans une plage de dates
    public List<RainForecast> getForecastsBySensorIdAndDateRange(String sensorId, Date startDate, Date endDate) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", supabaseKey);
        headers.set("Authorization", "Bearer " + supabaseKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        String start = toISO(startDate);
        String end = toISO(endDate);

        // Filtrer par sensor_id ET plage de dates (forecast_from >= start AND forecast_to <= end)
        String url = supabaseUrl + "/rest/v1/rain_forecast?" +
                "sensor_id=eq." + sensorId +
                "&forecast_from=gte." + start +
                "&forecast_to=lte." + end +
                "&order=forecast_from.desc";

        ResponseEntity<List<RainForecast>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<RainForecast>>() {}
        );

        return response.getBody();
    }



}
