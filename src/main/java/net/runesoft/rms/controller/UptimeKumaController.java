package net.runesoft.rms.controller;

import lombok.extern.slf4j.Slf4j;
import net.runesoft.rms.entity.UptimeAlert;
import net.runesoft.rms.model.UptimeKumaWebhookDTO;
import net.runesoft.rms.service.UptimeKumaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/uptime")
public class UptimeKumaController {

    @Autowired
    private UptimeKumaService uptimeKumaService;

    @PostMapping("/webhook")
    public ResponseEntity<String> receiveWebhook(@RequestBody UptimeKumaWebhookDTO webhook) {
        try {
            log.info("Webhook recibido desde Uptime Kuma: {}", webhook.getMonitorName());
            
            UptimeAlert alert = uptimeKumaService.processWebhook(webhook);
            
            return ResponseEntity.ok("Webhook procesado correctamente. ID: " + alert.getId());
        } catch (Exception e) {
            log.error("Error procesando webhook: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error procesando webhook: " + e.getMessage());
        }
    }

    @GetMapping("/alerts")
    public ResponseEntity<List<UptimeAlert>> getAllAlerts() {
        try {
            List<UptimeAlert> alerts = uptimeKumaService.getAllAlerts();
            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            log.error("Error obteniendo alertas: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/alerts/recent")
    public ResponseEntity<List<UptimeAlert>> getRecentAlerts() {
        try {
            List<UptimeAlert> alerts = uptimeKumaService.getRecentAlerts();
            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            log.error("Error obteniendo alertas recientes: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/alerts/monitor/{monitorName}")
    public ResponseEntity<List<UptimeAlert>> getAlertsByMonitor(@PathVariable String monitorName) {
        try {
            List<UptimeAlert> alerts = uptimeKumaService.getAlertsByMonitor(monitorName);
            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            log.error("Error obteniendo alertas del monitor {}: {}", monitorName, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/alerts/status/{status}")
    public ResponseEntity<List<UptimeAlert>> getAlertsByStatus(@PathVariable String status) {
        try {
            List<UptimeAlert> alerts = uptimeKumaService.getAlertsByStatus(status);
            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            log.error("Error obteniendo alertas con estado {}: {}", status, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
