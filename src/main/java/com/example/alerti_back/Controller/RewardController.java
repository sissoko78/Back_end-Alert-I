package com.example.alerti_back.Controller;

import com.example.alerti_back.Model.Reward;
import com.example.alerti_back.Service.RewardService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/rewards")
@CrossOrigin(
        origins = {"http://localhost:4200", "http://localhost:3000"},
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
                RequestMethod.PATCH, RequestMethod.DELETE, RequestMethod.OPTIONS},
        allowedHeaders = "*",
        allowCredentials = "false" // Changé de "true" à "false"
)
public class RewardController {

    private final RewardService rewardService;

    public RewardController(RewardService rewardService) {
        this.rewardService = rewardService;
    }

    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addReward(
            @RequestPart("reward") Reward reward,
            @RequestPart(value = "image", required = false) MultipartFile imageFile
    ) {
        try {
            Reward savedReward = rewardService.addRewardWithImage(reward, imageFile);
            return ResponseEntity.ok(savedReward);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Reward>> getAllRewards() {
        try {
            List<Reward> rewards = rewardService.getAllRewards();
            return ResponseEntity.ok(rewards);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reward> getRewardById(@PathVariable int id) {
        try {
            Reward reward = rewardService.getRewardById(id);
            if (reward == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.ok(reward);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Reward> updateReward(@PathVariable int id, @RequestBody Reward reward) {
        try {
            Reward existing = rewardService.getRewardById(id);
            if (existing == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            Reward updated = rewardService.updateReward(id, reward);
            return ResponseEntity.ok(updated);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReward(@PathVariable int id) {
        try {
            Reward existing = rewardService.getRewardById(id);
            if (existing == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            rewardService.deleteReward(id);
            return ResponseEntity.noContent().build();
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}