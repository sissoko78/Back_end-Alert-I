package com.example.alerti_back.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration pour activer les tâches programmées (@Scheduled)
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
    // Cette classe active les annotations @Scheduled dans l'application
}