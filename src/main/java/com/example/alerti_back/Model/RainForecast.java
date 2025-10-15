package com.example.alerti_back.Model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.Date;

public class RainForecast {
    private String id;              // UUID généré par Supabase
    @JsonProperty("sensor_id")
    private String sensorId;

    @JsonProperty("pluie_prevue")
    private Double pluiePrevue;

    @JsonProperty("forecast_from")
    private Date forecastFrom;

    @JsonProperty("forecast_to")
    private Date forecastTo;

    @JsonProperty("created_at")
    private Date createdAt;        // Pour savoir quand la requête a été faite

    @JsonProperty("forecast_block")
    private String forecastBlock;  // Nouveau attribut pour stocker le bloc (ex: "00:00-03:00")


    // Getters et Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSensorId() {
        return sensorId;
    }

    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }

    public Double getPluiePrevue() {
        return pluiePrevue;
    }

    public void setPluiePrevue(Double pluiePrevue) {
        this.pluiePrevue = pluiePrevue;
    }

    public Date getForecastFrom() { return forecastFrom; }
    public void setForecastFrom(Date forecastFrom) { this.forecastFrom = forecastFrom; }




    public Date getForecastTo() {
        return forecastTo;
    }

    public void setForecastTo(Date forecastTo) {
        this.forecastTo = forecastTo;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getForecastBlock() {
        return forecastBlock;
    }

    public void setForecastBlock(String forecastBlock) {
        this.forecastBlock = forecastBlock;
    }
}
