package com.example.alerti_back.Controller;

import com.example.alerti_back.Model.RainForecast;
import com.example.alerti_back.Service.RainForecastService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/forecast")
@CrossOrigin(origins = "*") // à adapter selon ta config CORS
public class RainForecastController {

    @Autowired
    private RainForecastService rainForecastService;

    // ✅ Récupérer toutes les prévisions pour un capteur
    @GetMapping("/{sensorId}")
    public List<RainForecast> getForecastsBySensorId(@PathVariable String sensorId) {
        return rainForecastService.getForecastsBySensorId(sensorId);
    }

    // ✅ Récupérer les prévisions filtrées par plage de dates
    @GetMapping("/{sensorId}/filter")
    public List<RainForecast> getForecastsBySensorIdAndDateRange(
            @PathVariable String sensorId,
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date end
    ) {
        return rainForecastService.getForecastsBySensorIdAndDateRange(sensorId, start, end);
    }
}
