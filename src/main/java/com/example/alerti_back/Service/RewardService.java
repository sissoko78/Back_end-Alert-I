package com.example.alerti_back.Service;

import com.example.alerti_back.Model.Reward;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

/**
 * Service pour gérer les opérations CRUD sur les récompenses (Reward),
 * via l’API REST de Supabase (PostgREST).
 */
@Service
public class RewardService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Prépare les en-têtes HTTP nécessaires pour les requêtes à Supabase.
     * Inclut la clé, l’autorisation, le content-type JSON et « return=representation »
     * pour que Supabase renvoie l’objet créé/modifié.
     */
    // Méthode pour créer les headers d'autorisation
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + supabaseKey);
        headers.set("apikey", supabaseKey);
        headers.set("Prefer", "return=representation");
        return headers;
    }

    /**
     * Crée une nouvelle récompense dans la table rewards.
     * @param reward L’objet Reward à créer (les champs non null doivent être fournis).
     * @return Le Reward créé (avec id, created_at, etc.)
     */
    public Reward addRewardWithImage(Reward reward, MultipartFile imageFile) throws IOException {
        String imageUrl = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            imageUrl = uploadImageToSupabase(imageFile);
        }

        String url = supabaseUrl + "/rest/v1/rewards";

        Map<String, Object> body = new HashMap<>();
        body.put("name", reward.getName());
        body.put("description", reward.getDescription());
        body.put("points_required", reward.getPoints_required());
        body.put("category", reward.getCategory());
        body.put("image_url", imageUrl);
        body.put("is_active", reward.isIs_active());
        body.put("stock", reward.getStock());

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, createHeaders());

        try {
            ResponseEntity<List<Reward>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<List<Reward>>() {}
            );

            if (response.getBody() != null && !response.getBody().isEmpty()) {
                return response.getBody().get(0);
            } else {
                throw new RuntimeException("Aucune récompense retournée par Supabase");
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'ajout de la récompense : " + e.getMessage());
            throw e;
        }
    }


    /**
     * Récupère toutes les récompenses.
     * @return Liste de Reward
     */
    public List<Reward> getAllRewards() {
        String url = supabaseUrl + "/rest/v1/rewards?select=*";

        HttpEntity<String> request = new HttpEntity<>(createHeaders());

        ResponseEntity<Reward[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                Reward[].class
        );

        return Arrays.asList(response.getBody());
    }

    /**
     * Récupère une récompense par son ID.
     * @param id L’ID de la récompense
     * @return Le Reward correspondant, ou null si non trouvé
     */
    public Reward getRewardById(int id) {
        String url = supabaseUrl + "/rest/v1/rewards?id=eq." + id + "&select=*";

        HttpEntity<String> request = new HttpEntity<>(createHeaders());

        ResponseEntity<Reward[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                Reward[].class
        );

        Reward[] rewards = response.getBody();
        return (rewards != null && rewards.length > 0) ? rewards[0] : null;
    }

    /**
     * Met à jour une récompense existante (PATCH partiel).
     * @param id L’ID de la récompense à modifier
     * @param reward L’objet Reward avec les champs à mettre à jour
     * @return Le Reward modifié
     */
    public Reward updateReward(int id, Reward reward) {
        String url = supabaseUrl + "/rest/v1/rewards?id=eq." + id;

        Map<String, Object> body = new HashMap<>();
        if (reward.getName() != null) body.put("name", reward.getName());
        if (reward.getDescription() != null) body.put("description", reward.getDescription());
        if (reward.getPoints_required() != null) body.put("points_required", Integer.parseInt(reward.getPoints_required()));
        if (reward.getCategory() != null) body.put("category", reward.getCategory());
        if (reward.getImage_url() != null) body.put("image_url", reward.getImage_url());
        body.put("is_active", reward.isIs_active());
        body.put("stock", reward.getStock());
        body.put("updated_at", new Date());  // on met à jour la date de modification

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, createHeaders());

        ResponseEntity<List<Reward>> response = restTemplate.exchange(
                url,
                HttpMethod.PATCH,
                request,
                new ParameterizedTypeReference<List<Reward>>() {}
        );

        return response.getBody().get(0);
    }

    /**
     * Supprime une récompense par son ID.
     * @param id L’ID de la récompense à supprimer
     */
    public void deleteReward(int id) {
        String url = supabaseUrl + "/rest/v1/rewards?id=eq." + id;

        HttpEntity<String> request = new HttpEntity<>(createHeaders());

        restTemplate.exchange(
                url,
                HttpMethod.DELETE,
                request,
                Void.class
        );
    }

    public String uploadImageToSupabase(MultipartFile file) throws IOException {
        String bucket = "rewards";
        String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename();
        String url = supabaseUrl + "/storage/v1/object/" + bucket + "/" + fileName;


        HttpHeaders headers = new HttpHeaders();
        // Utilisez le type MIME du fichier au lieu de MULTIPART_FORM_DATA
        headers.setContentType(MediaType.parseMediaType(file.getContentType()));
        headers.set("Authorization", "Bearer " + supabaseKey);
        headers.set("apikey", supabaseKey); // Ajoutez aussi l'apikey
        System.out.println("Headers: " + headers);
        System.out.println("File size: " + file.getSize());
        System.out.println("File type: " + file.getContentType());

        HttpEntity<byte[]> requestEntity = new HttpEntity<>(file.getBytes(), headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            // Vérifiez la réponse
            if (response.getStatusCode().is2xxSuccessful()) {
                return supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + fileName;
            } else {
                throw new RuntimeException("Échec de l'upload: " + response.getStatusCode());

            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'upload de l'image : " + e.getMessage());
            throw new IOException("Impossible d'uploader l'image", e);

        }


    }
}
