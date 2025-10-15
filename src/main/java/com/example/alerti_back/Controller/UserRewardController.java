package com.example.alerti_back.Controller;

import com.example.alerti_back.Model.UserReward;
import com.example.alerti_back.Service.UserRewardService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user-rewards")
public class UserRewardController {

    private final UserRewardService userRewardService;

    public UserRewardController(UserRewardService userRewardService) {
        this.userRewardService = userRewardService;
    }

    /**
     * Assigne une récompense à un utilisateur (création de la liaison).
     * @param userReward L’objet UserReward reçu du front
     * @return ResponseEntity contenant l’objet créé
     */
    @PostMapping
    public ResponseEntity<UserReward> assignReward(@RequestBody UserReward userReward) {
        try {
            UserReward created = userRewardService.assignRewardToUser(userReward);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les liaisons récompense/utilisateur pour un utilisateur donné.
     * @param userId L’identifiant utilisateur
     * @return ResponseEntity contenant la liste de UserReward
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserReward>> getUserRewards(@PathVariable String userId) {
        try {
            List<UserReward> list = userRewardService.getUserRewards(userId);
            return ResponseEntity.ok(list);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Met à jour le statut d’une liaison user_reward (ex: pending → claimed).
     * @param id L’ID de la liaison à modifier
     * @param body Un JSON contenant le nouveau statut { "status": "claimed" }
     * @return ResponseEntity avec l’objet modifié ou erreur
     */
    @PatchMapping("/{id}")
    public ResponseEntity<UserReward> updateUserRewardStatus(
            @PathVariable int id,
            @RequestBody java.util.Map<String, String> body) {
        try {
            String newStatus = body.get("status");
            if (newStatus == null) {
                return ResponseEntity.badRequest().build();  // 400 Bad Request
            }
            UserReward updated = userRewardService.updateUserRewardStatus(id, newStatus);
            return ResponseEntity.ok(updated);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Supprime une liaison user_reward.
     * @param id L’ID de la liaison à supprimer
     * @return ResponseEntity sans contenu ou erreur
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserReward(@PathVariable int id) {
        try {
            // On pourrait vérifier qu’il existe avant suppression
            userRewardService.deleteUserReward(id);
            return ResponseEntity.noContent().build();
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
