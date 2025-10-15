package com.example.alerti_back.Service;

import com.example.alerti_back.Model.HistoryEntry;
import com.example.alerti_back.Model.RainForecast;
import com.example.alerti_back.Model.Sensors;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

@Service
public class MosquittoListenerService {

    @Value("${mosquitto.broker}")
    private String brokerUrl; // tcp://44.196.139.190:1884

    @Value("${mosquitto.topic}")
    private String topic;     // alerti/sensors/#

    @Autowired
    private SupabaseService supabaseService;

    @Autowired
    private RainForecastService rainForecastService;

    @Autowired
    private SensorService sensorService;

    @Autowired
    private WeatherService weatherService;

    @Autowired
    private AlertService alertService;

    // ✅ NOUVEAU : Injection du service de planification météo
    @Autowired
    private WeatherSchedulerService weatherSchedulerService;

    private MqttClient client;

    @PostConstruct
    public void start() {
        try {
            // Création du client MQTT
            client = new MqttClient(brokerUrl, MqttClient.generateClientId());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true); // session propre à chaque démarrage

            // Connexion au broker Mosquitto
            client.connect(options);

            // Abonnement au topic
            client.subscribe(topic, (t, message) -> handleMessage(new String(message.getPayload())));

            System.out.println("✅ Connecté à Mosquitto : " + brokerUrl + " | Topic : " + topic);

        } catch (Exception e) {
            System.err.println("❌ Erreur connexion Mosquitto : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleMessage(String json) {
        System.out.println("📩 Message Mosquitto reçu : " + json);
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(json);

            String deviceId = node.get("device_id").asText();

            // Récupérer ou créer capteur
            Sensors sensor = supabaseService.findById(deviceId).orElse(new Sensors());
            sensor.setId(deviceId);
            sensor.setStatut("active");

            // Timestamp
            double timestampSeconds = node.get("timestamp").asDouble();
            Date timestampDate = (timestampSeconds < 1_000_000_000)
                    ? new Date()
                    : new Date((long) (timestampSeconds * 1000));

            sensor.setTimestamp(timestampDate);
            sensor.setUpdatedAt(new Date());

            // Données capteur
            sensor.setDernierDonneeCaptemperature(getDoubleOrNull(node, "Temperature"));
            sensor.setDernierDonneeCapniveauEau(getDoubleOrNull(node, "Niveau_eau"));
            sensor.setDernierDonneevitesseDuVent(getDoubleOrNull(node, "Vitesse_du_vent"));

            // Localisation
            sensor.setLatitude(getDoubleOrNull(node, "latitude"));
            sensor.setLongitude(getDoubleOrNull(node, "longitude"));

            // Historique
            HistoryEntry entry = new HistoryEntry();
            entry.setTimestamp(timestampDate);
            entry.setTemperature(sensor.getDernierDonneeCaptemperature());
            entry.setNiveauEau(sensor.getDernierDonneeCapniveauEau());
            entry.setVitesseDuVent(sensor.getDernierDonneevitesseDuVent());

            List<HistoryEntry> history = sensor.getHistory();
            if (history == null) {
                history = new ArrayList<>();
            }
            history.add(entry);
            sensor.setHistory(history);

            // ✅ TRAITEMENT MÉTÉO : Toujours afficher les informations météo si on a les coordonnées
            Double niveauActuel = sensor.getDernierDonneeCapniveauEau();
            Double seuil = sensor.getSeuilniveauEau();

            if (sensor.getLatitude() != null && sensor.getLongitude() != null) {
                try {
                    // ✅ Récupérer la prévision depuis le cache (pas d'appel API)
                    RainForecast currentForecast = weatherSchedulerService.getCurrentForecast(deviceId);

                    if (currentForecast != null) {
                        double pluiePrevue = currentForecast.getPluiePrevue();

                        System.out.println("🌧️ Pluie prévue (mm)     : " + pluiePrevue + " [Cache]");
                        System.out.println("🌊 Niveau actuel (mm)    : " + (niveauActuel != null ? niveauActuel : "Non disponible"));
                        System.out.println("🚧 Seuil du capteur (mm) : " + (seuil != null ? seuil : "Non configuré"));
                        System.out.println("📅 Prévision valable jusqu'à : " + currentForecast.getForecastTo());

                        // ✅ CALCUL ET ALERTE seulement si on a TOUS les paramètres nécessaires
                        if (niveauActuel != null && seuil != null) {
                            double totalAnticipe = niveauActuel + pluiePrevue;
                            System.out.println("🧮 Total anticipé (mm)   : " + totalAnticipe);

                            if (totalAnticipe >= seuil) {
                                System.out.println("🚨 INONDATION ANTICIPÉE pour le capteur " + deviceId);
                                alertService.sendAlert(deviceId,
                                        "⚠️ Risque d'inondation imminent ! Niveau estimé : " +
                                                String.format("%.2f", totalAnticipe) + " mm (seuil : " + seuil + " mm)");
                            } else {
                                System.out.println("✅ Niveau sous le seuil, pas d'alerte nécessaire");
                            }
                        } else {
                            // ✅ Afficher les raisons pour lesquelles on ne peut pas calculer l'alerte
                            if (niveauActuel == null) {
                                System.out.println("⚠️ Impossible de calculer l'alerte : Niveau d'eau non disponible");
                            }
                            if (seuil == null) {
                                System.out.println("⚠️ Impossible de calculer l'alerte : Seuil non configuré");
                            }
                        }

                    } else {
                        System.out.println("⚠️ Aucune prévision en cache pour le capteur " + deviceId +
                                " - Tentative de récupération via API de secours");

                        // ✅ Fallback : appel API si aucune prévision en cache (cas d'exception)
                        double pluiePrevue = weatherService.getRainForecast(sensor.getLatitude(), sensor.getLongitude());

                        System.out.println("🌧️ Pluie prévue (mm)     : " + pluiePrevue + " [API Fallback]");
                        System.out.println("🌊 Niveau actuel (mm)    : " + (niveauActuel != null ? niveauActuel : "Non disponible"));
                        System.out.println("🚧 Seuil du capteur (mm) : " + (seuil != null ? seuil : "Non configuré"));

                        // ✅ CALCUL ET ALERTE seulement si on a TOUS les paramètres nécessaires
                        if (niveauActuel != null && seuil != null) {
                            double totalAnticipe = niveauActuel + pluiePrevue;
                            System.out.println("🧮 Total anticipé (mm)   : " + totalAnticipe);

                            if (totalAnticipe >= seuil) {
                                System.out.println("🚨 INONDATION ANTICIPÉE pour le capteur " + deviceId + " [Fallback]");
                                alertService.sendAlert(deviceId,
                                        "⚠️ Risque d'inondation imminent ! Niveau estimé : " +
                                                String.format("%.2f", totalAnticipe) + " mm (seuil : " + seuil + " mm)");
                            } else {
                                System.out.println("✅ Niveau sous le seuil, pas d'alerte nécessaire [Fallback]");
                            }
                        } else {
                            // ✅ Afficher les raisons pour lesquelles on ne peut pas calculer l'alerte
                            if (niveauActuel == null) {
                                System.out.println("⚠️ Impossible de calculer l'alerte : Niveau d'eau non disponible [Fallback]");
                            }
                            if (seuil == null) {
                                System.out.println("⚠️ Impossible de calculer l'alerte : Seuil non configuré [Fallback]");
                            }
                        }
                    }

                } catch (Exception e) {
                    System.err.println("❌ Erreur lors du traitement des prévisions pour capteur " + deviceId + " : " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("⚠️ Capteur " + deviceId + " sans coordonnées GPS - Impossible de récupérer les prévisions météo");
            }

            // ✅ Sauvegarde finale
            supabaseService.saveSensorData(sensor);

        } catch (Exception e) {
            System.err.println("❌ Erreur traitement message Mosquitto : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Double getDoubleOrNull(JsonNode node, String field) {
        return node.has(field) && !node.get(field).isNull() ? node.get(field).asDouble() : null;
    }
}