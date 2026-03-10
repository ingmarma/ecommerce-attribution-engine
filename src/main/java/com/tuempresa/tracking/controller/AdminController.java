package com.tuempresa.tracking.controller;

// CAMBIÁ ESTA LÍNEA:
import com.tuempresa.tracking.service.integration.DataSeederService; 

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {
    private final DataSeederService seederService;

    public AdminController(DataSeederService seederService) {
        this.seederService = seederService;
    }

    @GetMapping("/seed")
    public String seed(@RequestParam(defaultValue = "20") int days) {
        seederService.seedHistoricalData(days);
        return "Simulación completada. Revisa Grafana y Pipedrive.";
    }
}
