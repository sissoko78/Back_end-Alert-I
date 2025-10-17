package com.example.alerti_back.Controller;

import com.example.alerti_back.Model.User;
import com.example.alerti_back.Service.AuthService;
import com.example.alerti_back.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public Map<String, String> register(@RequestBody User user) {
        System.out.println("📥 Reçu une requête d'inscription : " + user.getEmail());

        boolean created = authService.register(user);

        if (created) {
            return Map.of("message", "Utilisateur créé avec succès");
        } else {
            return Map.of("message", "Échec de la création de l'utilisateur");
        }
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> loginData) {
        String token = authService.login(loginData.get("email"), loginData.get("password"));
        return Map.of("token", token);
    }


}
