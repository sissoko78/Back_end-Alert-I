package com.example.alerti_back.Service;

import com.example.alerti_back.Model.SosSignal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Service pour gérer les signalements SOS/urgence des citoyens
 */
@Service
public class SosService {

    @Value("${supabase.url:}")
    private String supabaseUrl;

    @Value("${supabase.key:}")
    private String supabaseKey;



    private final RestTemplate restTemplate = new RestTemplate();
    private final SimpleDateFormat isoFormat;

    public SosService() {
        isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * Créer un nouveau signalement SOS
     */
    public SosSignal creerSignalement(SosSignal sosSignal) {
        try {
            // Validation des données
            validerSignalement(sosSignal);

            // Initialiser les valeurs par défaut
            if (sosSignal.getSignalTimestamp() == null) {
                sosSignal.setSignalTimestamp(new Date());
            }
            sosSignal.setUpdatedAt(new Date());

            if (sosSignal.getStatut() == null) {
                sosSignal.setStatut("en_cours");
            }

            // Sauvegarder dans Supabase
            Map<String, Object> payload = creerPayloadSupabase(sosSignal);

            // Debug: Afficher le payload envoyé à Supabase
            System.out.println("🔍 Payload envoyé à Supabase: " + payload);

            HttpHeaders headers = creerHeaders();
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            String url = supabaseUrl + "/rest/v1/sos_signals";
            ResponseEntity<Object[]> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, Object[].class);

            if (response.getStatusCode() == HttpStatus.CREATED ||
                    response.getStatusCode() == HttpStatus.OK) {

                System.out.println("✅ Signalement SOS créé avec succès pour l'utilisateur " + sosSignal.getUserId());



                return sosSignal;
            } else {
                throw new RuntimeException("Erreur lors de la création du signalement: " + response.getStatusCode());
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur création signalement SOS: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Impossible de créer le signalement SOS", e);
        }
    }

    /**
     * Récupérer tous les signalements
     */
    public List<SosSignal> obtenirTousSignalements() {
        try {
            HttpHeaders headers = creerHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String url = supabaseUrl + "/rest/v1/sos_signals?select=*&order=signal_timestamp.desc";
            ResponseEntity<Map[]> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, Map[].class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return convertirMapsEnSosSignals(Arrays.asList(response.getBody()));
            }

            return new ArrayList<>();

        } catch (Exception e) {
            System.err.println("❌ Erreur récupération signalements: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Récupérer les signalements par utilisateur
     */
    public List<SosSignal> obtenirSignalementsParUtilisateur(int userId) {
        try {
            HttpHeaders headers = creerHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String url = supabaseUrl + "/rest/v1/sos_signals?select=*&user_id=eq." + userId + "&order=signal_timestamp.desc";
            ResponseEntity<Map[]> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, Map[].class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return convertirMapsEnSosSignals(Arrays.asList(response.getBody()));
            }

            return new ArrayList<>();

        } catch (Exception e) {
            System.err.println("❌ Erreur récupération signalements utilisateur: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Récupérer un signalement par ID
     */
    public Optional<SosSignal> obtenirSignalementParId(int id) {
        try {
            HttpHeaders headers = creerHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String url = supabaseUrl + "/rest/v1/sos_signals?select=*&id=eq." + id;
            ResponseEntity<Map[]> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, Map[].class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null && response.getBody().length > 0) {
                return Optional.of(convertirMapEnSosSignal(response.getBody()[0]));
            }

            return Optional.empty();

        } catch (Exception e) {
            System.err.println("❌ Erreur récupération signalement par ID: " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Mettre à jour le statut d'un signalement
     */
    public boolean mettreAJourStatut(int signalId, String nouveauStatut) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("statut", nouveauStatut);
            payload.put("updated_at", isoFormat.format(new Date()));

            HttpHeaders headers = creerHeaders();
            headers.set("Prefer", "return=minimal");
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            String url = supabaseUrl + "/rest/v1/sos_signals?id=eq." + signalId;
            ResponseEntity<Void> response = restTemplate.exchange(
                    url, HttpMethod.PATCH, entity, Void.class);

            if (response.getStatusCode() == HttpStatus.NO_CONTENT ||
                    response.getStatusCode() == HttpStatus.OK) {

                System.out.println("✅ Statut du signalement " + signalId + " mis à jour: " + nouveauStatut);
                return true;
            }

            return false;

        } catch (Exception e) {
            System.err.println("❌ Erreur mise à jour statut signalement: " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtenir les statistiques des signalements
     */
    public Map<String, Object> obtenirStatistiques() {
        try {
            List<SosSignal> tousSignalements = obtenirTousSignalements();

            Map<String, Object> stats = new HashMap<>();
            stats.put("total", tousSignalements.size());

            // Compter par statut
            Map<String, Long> parStatut = new HashMap<>();
            parStatut.put("en_cours", tousSignalements.stream().filter(s -> "en_cours".equals(s.getStatut())).count());
            parStatut.put("traite", tousSignalements.stream().filter(s -> "traite".equals(s.getStatut())).count());
            parStatut.put("resolu", tousSignalements.stream().filter(s -> "resolu".equals(s.getStatut())).count());
            stats.put("par_statut", parStatut);

            // Compter par priorité
            Map<String, Long> parPriorite = new HashMap<>();
            parPriorite.put("faible", tousSignalements.stream().filter(s -> "faible".equals(s.getPriorite())).count());
            parPriorite.put("moyenne", tousSignalements.stream().filter(s -> "moyenne".equals(s.getPriorite())).count());
            parPriorite.put("haute", tousSignalements.stream().filter(s -> "haute".equals(s.getPriorite())).count());
            parPriorite.put("critique", tousSignalements.stream().filter(s -> "critique".equals(s.getPriorite())).count());
            stats.put("par_priorite", parPriorite);

            // Compter par type d'urgence
            Map<String, Long> parType = new HashMap<>();
            tousSignalements.stream()
                    .collect(java.util.stream.Collectors.groupingBy(
                            SosSignal::getTypeUrgence,
                            java.util.stream.Collectors.counting()))
                    .forEach(parType::put);
            stats.put("par_type", parType);

            return stats;

        } catch (Exception e) {
            System.err.println("❌ Erreur calcul statistiques: " + e.getMessage());
            return new HashMap<>();
        }
    }

    // Méthodes privées utilitaires

    private void validerSignalement(SosSignal sosSignal) {
        if (sosSignal.getTypeUrgence() == null || sosSignal.getTypeUrgence().trim().isEmpty()) {
            throw new IllegalArgumentException("Le type d'urgence est obligatoire");
        }
        if (sosSignal.getDescription() == null || sosSignal.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("La description est obligatoire");
        }
        if (sosSignal.getLocalite() == null || sosSignal.getLocalite().trim().isEmpty()) {
            throw new IllegalArgumentException("La localité est obligatoire");
        }
        if (sosSignal.getLatitude() == null || sosSignal.getLongitude() == null) {
            throw new IllegalArgumentException("Les coordonnées GPS sont obligatoires");
        }
    }

    private Map<String, Object> creerPayloadSupabase(SosSignal sosSignal) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("user_id", sosSignal.getUserId());
        payload.put("type_urgence", sosSignal.getTypeUrgence());
        payload.put("description", sosSignal.getDescription());
        payload.put("localite", sosSignal.getLocalite());
        payload.put("latitude", sosSignal.getLatitude());
        payload.put("longitude", sosSignal.getLongitude());
        payload.put("statut", sosSignal.getStatut());
        payload.put("priorite", sosSignal.getPriorite());
        payload.put("signal_timestamp", isoFormat.format(sosSignal.getSignalTimestamp()));
        payload.put("updated_at", isoFormat.format(sosSignal.getUpdatedAt()));
        payload.put("photo_url", sosSignal.getPhotoUrl());
        payload.put("numero_urgence", sosSignal.getNumeroUrgence());
        payload.put("anonyme", sosSignal.isAnonyme());
        return payload;
    }

    private HttpHeaders creerHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", supabaseKey);
        headers.set("Authorization", "Bearer " + supabaseKey);
        headers.set("Prefer", "return=representation");
        return headers;
    }


    private List<SosSignal> convertirMapsEnSosSignals(List<Map<String, Object>> maps) {
        List<SosSignal> signals = new ArrayList<>();
        for (Map<String, Object> map : maps) {
            signals.add(convertirMapEnSosSignal(map));
        }
        return signals;
    }

    private SosSignal convertirMapEnSosSignal(Map<String, Object> map) {
        // Debug: Afficher les données de chaque signalement
        System.out.println("🔍 Map récupéré de Supabase: " + map);

        SosSignal signal = new SosSignal();
        signal.setId((Integer) map.get("id"));
        signal.setUserId((Integer) map.get("user_id"));
        signal.setTypeUrgence((String) map.get("type_urgence"));

        // Debug: Afficher le type d'urgence récupéré
        System.out.println("🔍 Type d'urgence récupéré: " + map.get("type_urgence"));
        signal.setDescription((String) map.get("description"));
        signal.setLocalite((String) map.get("localite"));
        signal.setLatitude(map.get("latitude") != null ? ((Number) map.get("latitude")).doubleValue() : null);
        signal.setLongitude(map.get("longitude") != null ? ((Number) map.get("longitude")).doubleValue() : null);
        signal.setStatut((String) map.get("statut"));
        signal.setPriorite((String) map.get("priorite"));
        signal.setPhotoUrl((String) map.get("photo_url"));
        signal.setNumeroUrgence((String) map.get("numero_urgence"));
        signal.setAnonyme(map.get("anonyme") != null ? (Boolean) map.get("anonyme") : false);

        // Parsing des dates
        try {
            if (map.get("signal_timestamp") != null) {
                signal.setSignalTimestamp(isoFormat.parse((String) map.get("signal_timestamp")));
            }
            if (map.get("updated_at") != null) {
                signal.setUpdatedAt(isoFormat.parse((String) map.get("updated_at")));
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur parsing dates: " + e.getMessage());
        }

        return signal;
    }
}