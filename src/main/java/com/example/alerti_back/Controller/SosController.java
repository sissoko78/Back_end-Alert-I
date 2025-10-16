package com.example.alerti_back.Controller;

import com.example.alerti_back.Model.SosSignal;
import com.example.alerti_back.Service.SosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Contrôleur REST pour les signalements SOS/urgence des citoyens
 */
@RestController
@RequestMapping("/api/sos")
@CrossOrigin(origins = "*")
public class SosController {

    @Autowired
    private SosService sosService;

    /**
     * Créer un nouveau signalement SOS
     * POST /api/sos/signal
     */
    @PostMapping("/signal")
    public ResponseEntity<?> creerSignalement(@RequestBody SosSignal sosSignal) {
        try {
            SosSignal signalCree = sosService.creerSignalement(sosSignal);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Signalement SOS créé avec succès");
            response.put("data", signalCree);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Données invalides: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Erreur lors de la création du signalement: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Créer un signalement SOS anonyme (sans authentification)
     * POST /api/sos/signal-anonyme
     */
    @PostMapping("/signal-anonyme")
    public ResponseEntity<?> creerSignalementAnonyme(@RequestBody Map<String, Object> signalData) {
        try {
            // Debug: Afficher les données reçues
            System.out.println("🔍 Données reçues pour signalement anonyme: " + signalData);

            SosSignal sosSignal = new SosSignal();
            sosSignal.setUserId(0); // ID 0 pour les signalements anonymes
            sosSignal.setTypeUrgence((String) signalData.get("typeUrgence"));
            sosSignal.setDescription((String) signalData.get("description"));
            sosSignal.setLocalite((String) signalData.get("localite"));
            sosSignal.setLatitude(((Number) signalData.get("latitude")).doubleValue());
            sosSignal.setLongitude(((Number) signalData.get("longitude")).doubleValue());
            sosSignal.setPriorite((String) signalData.getOrDefault("priorite", "moyenne"));
            sosSignal.setAnonyme(true);

            // Debug: Afficher le type d'urgence avant sauvegarde
            System.out.println("🔍 Type d'urgence avant sauvegarde: " + sosSignal.getTypeUrgence());

            if (signalData.get("photoUrl") != null) {
                sosSignal.setPhotoUrl((String) signalData.get("photoUrl"));
            }
            if (signalData.get("numeroUrgence") != null) {
                sosSignal.setNumeroUrgence((String) signalData.get("numeroUrgence"));
            }

            SosSignal signalCree = sosService.creerSignalement(sosSignal);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Signalement anonyme créé avec succès");
            response.put("data", signalCree);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Erreur lors de la création du signalement anonyme: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Récupérer tous les signalements (pour les administrateurs)
     * GET /api/sos/signaux
     */
    @GetMapping("/signaux")
    public ResponseEntity<?> obtenirTousSignalements() {
        try {
            List<SosSignal> signalements = sosService.obtenirTousSignalements();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", signalements);
            response.put("total", signalements.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Erreur lors de la récupération des signalements: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Récupérer les signalements d'un utilisateur
     * GET /api/sos/signaux/user/{userId}
     */
    @GetMapping("/signaux/user/{userId}")
    public ResponseEntity<?> obtenirSignalementsUtilisateur(@PathVariable int userId) {
        try {
            List<SosSignal> signalements = sosService.obtenirSignalementsParUtilisateur(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", signalements);
            response.put("total", signalements.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Erreur lors de la récupération des signalements utilisateur: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Récupérer un signalement par ID
     * GET /api/sos/signal/{id}
     */
    @GetMapping("/signal/{id}")
    public ResponseEntity<?> obtenirSignalementParId(@PathVariable int id) {
        try {
            Optional<SosSignal> signalement = sosService.obtenirSignalementParId(id);

            if (signalement.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("data", signalement.get());
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Signalement non trouvé");
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Erreur lors de la récupération du signalement: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Mettre à jour le statut d'un signalement (pour les administrateurs)
     * PATCH /api/sos/signal/{id}/statut
     */
    @PatchMapping("/signal/{id}/statut")
    public ResponseEntity<?> mettreAJourStatut(@PathVariable int id, @RequestBody Map<String, String> request) {
        try {
            String nouveauStatut = request.get("statut");

            if (nouveauStatut == null || nouveauStatut.trim().isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Le nouveau statut est obligatoire");
                return ResponseEntity.badRequest().body(error);
            }

            boolean misAJour = sosService.mettreAJourStatut(id, nouveauStatut);

            if (misAJour) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Statut mis à jour avec succès");
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Impossible de mettre à jour le statut");
                return ResponseEntity.internalServerError().body(error);
            }

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Erreur lors de la mise à jour du statut: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Obtenir les statistiques des signalements
     * GET /api/sos/statistiques
     */
    @GetMapping("/statistiques")
    public ResponseEntity<?> obtenirStatistiques() {
        try {
            Map<String, Object> statistiques = sosService.obtenirStatistiques();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", statistiques);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Erreur lors du calcul des statistiques: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Obtenir les signalements urgents (priorité haute ou critique)
     * GET /api/sos/urgents
     */
    @GetMapping("/urgents")
    public ResponseEntity<?> obtenirSignalementsUrgents() {
        try {
            List<SosSignal> tousSignalements = sosService.obtenirTousSignalements();
            List<SosSignal> urgents = tousSignalements.stream()
                    .filter(SosSignal::isUrgent)
                    .toList();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", urgents);
            response.put("total", urgents.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Erreur lors de la récupération des signalements urgents: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Obtenir les types d'urgence disponibles
     * GET /api/sos/types-urgence
     */
    @GetMapping("/types-urgence")
    public ResponseEntity<?> obtenirTypesUrgence() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", List.of(
                Map.of("code", "inondation", "label", "Inondation", "description", "Risque ou présence d'inondation"),
                Map.of("code", "accident", "label", "Accident", "description", "Accident de la route ou autre"),
                Map.of("code", "incendie", "label", "Incendie", "description", "Début ou présence d'incendie"),
                Map.of("code", "agression", "label", "Agression", "description", "Violence ou agression"),
                Map.of("code", "blessure", "label", "Blessure", "description", "Personne blessée nécessitant assistance"),
                Map.of("code", "infrastructure", "label", "Infrastructure", "description", "Problème d'infrastructure publique"),
                Map.of("code", "pollution", "label", "Pollution", "description", "Pollution ou déversement dangereux"),
                Map.of("code", "autre", "label", "Autre", "description", "Autre type d'urgence")
        ));

        return ResponseEntity.ok(response);
    }

    /**
     * Obtenir les priorités disponibles
     * GET /api/sos/priorites
     */
    @GetMapping("/priorites")
    public ResponseEntity<?> obtenirPriorites() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", List.of(
                Map.of("code", "faible", "label", "Faible", "description", "Urgence mineure", "couleur", "green"),
                Map.of("code", "moyenne", "label", "Moyenne", "description", "Urgence modérée", "couleur", "orange"),
                Map.of("code", "haute", "label", "Haute", "description", "Urgence importante", "couleur", "red"),
                Map.of("code", "critique", "label", "Critique", "description", "Urgence critique", "couleur", "darkred")
        ));

        return ResponseEntity.ok(response);
    }

    /**
     * Obtenir les statuts disponibles
     * GET /api/sos/statuts
     */
    @GetMapping("/statuts")
    public ResponseEntity<?> obtenirStatuts() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", List.of(
                Map.of("code", "en_cours", "label", "En cours", "description", "Signalement en cours de traitement"),
                Map.of("code", "traite", "label", "Traité", "description", "Signalement pris en charge"),
                Map.of("code", "resolu", "label", "Résolu", "description", "Signalement résolu")
        ));

        return ResponseEntity.ok(response);
    }
}