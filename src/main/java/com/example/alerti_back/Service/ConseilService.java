package com.example.alerti_back.Service;

import com.example.alerti_back.Model.Conseil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConseilService {

    @Value("${supabase.url:https://wpmowqykjelftkptiquf.supabase.co}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public void ajouterConseil(Conseil conseil) {
        System.out.println("=== AJOUT CONSEIL DEBUG ===");
        System.out.println("Type: " + conseil.getType());
        System.out.println("Description: " + conseil.getDescription());
        System.out.println("URL: " + supabaseUrl);
        System.out.println("Key présente: " + (supabaseKey != null && !supabaseKey.isEmpty()));

        String conseilUrl = supabaseUrl + "/rest/v1/conseil";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", supabaseKey);
        headers.set("Authorization", "Bearer " + supabaseKey);
        headers.set("Prefer", "return=representation");

        // Debug headers
        System.out.println("Headers: " + headers.toString());

        Map<String, Object> conseilBody = new HashMap<>();
        conseilBody.put("type", conseil.getType());
        conseilBody.put("description", conseil.getDescription());

        System.out.println("Body: " + conseilBody.toString());

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(conseilBody, headers);

        try {
            ResponseEntity<Conseil[]> response = restTemplate.exchange(
                    conseilUrl,
                    HttpMethod.POST,
                    requestEntity,
                    Conseil[].class
            );

            System.out.println("Response Status: " + response.getStatusCode());
            System.out.println("Response Body: " + response.getBody());

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null && response.getBody().length > 0) {
                Conseil created = response.getBody()[0];
                System.out.println("Conseil créé avec ID : " + created.getId());
            } else {
                System.err.println("Réponse vide ou échec: " + response.getStatusCode());
            }

        } catch (Exception e) {
            System.err.println("Erreur lors de l'ajout du conseil:");
            System.err.println("Message: " + e.getMessage());
            System.err.println("Cause: " + (e.getCause() != null ? e.getCause().getMessage() : "Aucune"));
            e.printStackTrace();
            throw new RuntimeException("Échec de l'ajout du conseil: " + e.getMessage(), e);
        }
    }

    public List<Conseil> getConseilsParType(String type) {
        String url = supabaseUrl + "/rest/v1/conseil?type=eq." + type;

        HttpHeaders headers = createHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Conseil[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    Conseil[].class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return List.of(response.getBody());
            } else {
                throw new RuntimeException("Erreur lors de la récupération des conseils : " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.err.println("Erreur getConseilsParType: " + e.getMessage());
            throw new RuntimeException("Impossible de récupérer les conseils pour le type: " + type, e);
        }
    }

    public List<String> getTypesDistincts() {
        String url = supabaseUrl + "/rest/v1/conseil?select=type";

        HttpHeaders headers = createHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<List<Map<String, String>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<Map<String, String>>>() {}
            );

            if (response.getBody() == null) {
                return List.of();
            }

            // Extraire les types et supprimer les doublons
            return response.getBody()
                    .stream()
                    .map(map -> map.get("type"))
                    .filter(type -> type != null && !type.trim().isEmpty())
                    .distinct()
                    .toList();

        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des types : " + e.getMessage());
            e.printStackTrace();
            return List.of(); // Retourner liste vide au lieu d'exception
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", supabaseKey);
        headers.set("Authorization", "Bearer " + supabaseKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}