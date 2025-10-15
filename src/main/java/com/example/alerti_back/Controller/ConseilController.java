package com.example.alerti_back.Controller;

import com.example.alerti_back.Model.Conseil;
import com.example.alerti_back.Service.ConseilService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conseils")
@CrossOrigin(origins = "*") // Ajouter CORS si nécessaire
public class ConseilController {

    private final ConseilService conseilService;

    @Autowired
    public ConseilController(ConseilService conseilService) {
        this.conseilService = conseilService;
    }

    @PostMapping("/add_conseil")
    public ResponseEntity<String> ajouterConseil(@RequestBody Conseil conseil) {
        System.out.println("=== CONTROLLER DEBUG ===");
        System.out.println("Conseil reçu - Type: " + conseil.getType());
        System.out.println("Conseil reçu - Description: " + conseil.getDescription());
        System.out.println("Conseil reçu - ID: " + conseil.getId());

        // Validation côté controller
        if (conseil.getType() == null || conseil.getType().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Le type ne peut pas être vide");
        }

        if (conseil.getDescription() == null || conseil.getDescription().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("La description ne peut pas être vide");
        }

        try {
            conseilService.ajouterConseil(conseil);
            return ResponseEntity.ok("Conseil ajouté avec succès.");
        } catch (Exception e) {
            System.err.println("Erreur controller ajouterConseil: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity
                    .status(500)
                    .body("Erreur lors de l'ajout du conseil : " + e.getMessage());
        }
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Conseil>> getConseilsParType(@PathVariable String type) {
        System.out.println("Récupération conseils pour type: " + type);
        try {
            List<Conseil> conseils = conseilService.getConseilsParType(type);
            System.out.println("Nombre de conseils trouvés: " + conseils.size());
            return ResponseEntity.ok(conseils);
        } catch (Exception e) {
            System.err.println("Erreur getConseilsParType: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/types")
    public ResponseEntity<List<String>> getAllTypes() {
        System.out.println("Récupération de tous les types");
        try {
            List<String> types = conseilService.getTypesDistincts();
            System.out.println("Types trouvés: " + types);
            return ResponseEntity.ok(types);
        } catch (Exception e) {
            System.err.println("Erreur getAllTypes: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
}