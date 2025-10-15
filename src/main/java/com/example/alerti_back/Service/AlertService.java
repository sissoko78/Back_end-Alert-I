package com.example.alerti_back.Service;


import org.springframework.stereotype.Service;

@Service
public class AlertService {
    public void sendAlert(String deviceId, String message) {
        // à adapter selon ton système mobile, SMS, etc.
        System.out.println("📨 Alerte envoyée pour capteur " + deviceId + " : " + message);
    }
}
