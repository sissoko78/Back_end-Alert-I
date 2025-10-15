package com.example.alerti_back.Service;

import com.example.alerti_back.Model.UserReward;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Service pour gérer les opérations sur la table de liaison user_rewards,
 * qui lie un utilisateur à une récompense avec un statut, des dates, etc.
 */
@Service
public class UserRewardService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    private final RestTemplate restTemplate = new RestTemplate();

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", supabaseKey);
        headers.set("Authorization", "Bearer " + supabaseKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Prefer", "return=representation");
        return headers;
    }

    /**
     * Assigne une récompense à un utilisateur.
     * @param userReward L’objet UserReward décrivant la relation
     * @return Le UserReward créé (avec id, created_at, etc.)
     */
    public UserReward assignRewardToUser(UserReward userReward) {
        String url = supabaseUrl + "/rest/v1/user_rewards";

        Map<String, Object> body = new HashMap<>();
        body.put("user_id", userReward.getUser_id());
        body.put("reward_id", userReward.getReward_id());
        body.put("points_spent", userReward.getPoints_spent());
        body.put("status", userReward.getStatus());
        body.put("claimed_at", userReward.getClaimed_at());
        body.put("expires_at", userReward.getExpires_at());

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, createHeaders());

        ResponseEntity<List<UserReward>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<List<UserReward>>() {}
        );

        return response.getBody().get(0);
    }

    /**
     * Récupère toutes les liaisons récompense/utilisateur pour un utilisateur donné.
     * @param userId L’ID (ou identifiant string) de l’utilisateur
     * @return Liste de UserReward pour cet utilisateur
     */
    public List<UserReward> getUserRewards(String userId) {
        String url = supabaseUrl + "/rest/v1/user_rewards?user_id=eq." + userId + "&select=*";

        HttpEntity<String> request = new HttpEntity<>(createHeaders());

        ResponseEntity<UserReward[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                UserReward[].class
        );

        return Arrays.asList(response.getBody());
    }

    /**
     * Met à jour le statut d’une relation user_reward (ex : de “pending” à “claimed”).
     * @param userRewardId L’ID de la liaison à modifier
     * @param newStatus Le nouveau statut autorisé (pending, claimed, expired)
     * @return Le UserReward modifié
     */
    public UserReward updateUserRewardStatus(int userRewardId, String newStatus) {
        String url = supabaseUrl + "/rest/v1/user_rewards?id=eq." + userRewardId;

        Map<String, Object> body = new HashMap<>();
        body.put("status", newStatus);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, createHeaders());

        ResponseEntity<List<UserReward>> response = restTemplate.exchange(
                url,
                HttpMethod.PATCH,
                request,
                new ParameterizedTypeReference<List<UserReward>>() {}
        );

        return response.getBody().get(0);
    }

    /**
     * Supprime une liaison user_reward.
     * @param userRewardId L’ID de la ligne à supprimer
     */
    public void deleteUserReward(int userRewardId) {
        String url = supabaseUrl + "/rest/v1/user_rewards?id=eq." + userRewardId;

        HttpEntity<String> request = new HttpEntity<>(createHeaders());

        restTemplate.exchange(
                url,
                HttpMethod.DELETE,
                request,
                Void.class
        );
    }
}
