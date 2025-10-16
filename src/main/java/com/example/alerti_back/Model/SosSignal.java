package com.example.alerti_back.Model;

import java.util.Date;

/**
 * Modèle pour les signalements SOS/urgence des citoyens
 */
public class SosSignal {
    private int id;
    private int userId;           // ID de l'utilisateur qui signale
    private String typeUrgence;   // Type d'urgence (inondation, accident, incendie, etc.)
    private String description;   // Description détaillée du problème
    private String localite;      // Localité où se produit l'urgence
    private Double latitude;      // Coordonnées GPS
    private Double longitude;
    private String statut;        // statut (en_cours, traite, resolu)
    private String priorite;      // priorite (faible, moyenne, haute, critique)
    private Date signalTimestamp; // Date/heure du signalement
    private Date updatedAt;       // Dernière mise à jour
    private String photoUrl;      // URL de photo si disponible
    private String numeroUrgence; // Numéro de téléphone d'urgence
    private boolean anonyme;      // Signalement anonyme ou non

    // Constructeurs
    public SosSignal() {}

    public SosSignal(int userId, String typeUrgence, String description, String localite,
                     Double latitude, Double longitude, String priorite) {
        this.userId = userId;
        this.typeUrgence = typeUrgence;
        this.description = description;
        this.localite = localite;
        this.latitude = latitude;
        this.longitude = longitude;
        this.priorite = priorite;
        this.statut = "en_cours";
        this.signalTimestamp = new Date();
        this.updatedAt = new Date();
        this.anonyme = false;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getTypeUrgence() {
        return typeUrgence;
    }

    public void setTypeUrgence(String typeUrgence) {
        this.typeUrgence = typeUrgence;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocalite() {
        return localite;
    }

    public void setLocalite(String localite) {
        this.localite = localite;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
        this.updatedAt = new Date(); // Mise à jour automatique de la date
    }

    public String getPriorite() {
        return priorite;
    }

    public void setPriorite(String priorite) {
        this.priorite = priorite;
    }

    public Date getSignalTimestamp() {
        return signalTimestamp;
    }

    public void setSignalTimestamp(Date signalTimestamp) {
        this.signalTimestamp = signalTimestamp;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getNumeroUrgence() {
        return numeroUrgence;
    }

    public void setNumeroUrgence(String numeroUrgence) {
        this.numeroUrgence = numeroUrgence;
    }

    public boolean isAnonyme() {
        return anonyme;
    }

    public void setAnonyme(boolean anonyme) {
        this.anonyme = anonyme;
    }

    /**
     * Vérifie si le signalement nécessite une action urgente
     */
    public boolean isUrgent() {
        return "critique".equals(priorite) || "haute".equals(priorite);
    }

    /**
     * Vérifie si le signalement est résolu
     */
    public boolean isResolu() {
        return "resolu".equals(statut);
    }

    @Override
    public String toString() {
        return "SosSignal{" +
                "id=" + id +
                ", userId=" + userId +
                ", typeUrgence='" + typeUrgence + '\'' +
                ", localite='" + localite + '\'' +
                ", priorite='" + priorite + '\'' +
                ", statut='" + statut + '\'' +
                ", signalTimestamp=" + signalTimestamp +
                '}';
    }
}