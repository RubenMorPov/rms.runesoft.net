package net.runesoft.rms.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.runesoft.rms.config.RabbitMQConfig;
import net.runesoft.rms.entity.UptimeAlert;
import net.runesoft.rms.model.MessageDTO;
import net.runesoft.rms.model.UptimeKumaWebhookDTO;
import net.runesoft.rms.repository.UptimeAlertRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class UptimeKumaService {

    @Autowired
    private UptimeAlertRepository uptimeAlertRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired(required = false)
    private EmailService emailService;

    @Transactional
    public UptimeAlert processWebhook(UptimeKumaWebhookDTO webhook) {
        log.info("Procesando webhook de Uptime Kuma: {}", webhook.getMsg());

        // Crear entidad de alerta
        UptimeAlert alert = new UptimeAlert();
        
        // Parsear datos del heartbeat si está presente
        try {
            if (webhook.getHeartbeat() != null && !webhook.getHeartbeat().isEmpty()) {
                JsonNode heartbeatNode = objectMapper.readTree(webhook.getHeartbeat());
                alert.setHeartbeatData(webhook.getHeartbeat());
                
                // Extraer campos relevantes
                if (heartbeatNode.has("status")) {
                    int statusCode = heartbeatNode.get("status").asInt();
                    alert.setStatus(mapStatus(statusCode));
                }
                if (heartbeatNode.has("ping")) {
                    alert.setPing(heartbeatNode.get("ping").asInt());
                }
                if (heartbeatNode.has("time")) {
                    alert.setAlertTime(parseDateTime(heartbeatNode.get("time").asText()));
                }
            }
        } catch (Exception e) {
            log.warn("Error parseando heartbeat: {}", e.getMessage());
            alert.setHeartbeatData(webhook.getHeartbeat());
        }

        // Parsear datos del monitor si está presente
        try {
            if (webhook.getMonitor() != null && !webhook.getMonitor().isEmpty()) {
                JsonNode monitorNode = objectMapper.readTree(webhook.getMonitor());
                alert.setMonitorData(webhook.getMonitor());
                
                if (monitorNode.has("name")) {
                    alert.setMonitorName(monitorNode.get("name").asText());
                }
                if (monitorNode.has("url")) {
                    alert.setMonitorUrl(monitorNode.get("url").asText());
                }
            }
        } catch (Exception e) {
            log.warn("Error parseando monitor: {}", e.getMessage());
            alert.setMonitorData(webhook.getMonitor());
        }

        // Usar datos del DTO si no se pudieron parsear del JSON
        if (alert.getMonitorName() == null && webhook.getMonitorName() != null) {
            alert.setMonitorName(webhook.getMonitorName());
        }
        if (alert.getMonitorUrl() == null && webhook.getMonitorUrl() != null) {
            alert.setMonitorUrl(webhook.getMonitorUrl());
        }
        if (alert.getStatus() == null && webhook.getStatus() != null) {
            alert.setStatus(webhook.getStatus());
        }
        if (alert.getPing() == null && webhook.getPing() != null) {
            alert.setPing(webhook.getPing());
        }

        // Establecer mensaje
        alert.setMessage(webhook.getMsg() != null ? webhook.getMsg() : "Alerta de Uptime Kuma");

        // Guardar en base de datos
        UptimeAlert savedAlert = uptimeAlertRepository.save(alert);
        log.info("Alerta guardada en BD: ID={}, Monitor={}, Status={}", 
                savedAlert.getId(), savedAlert.getMonitorName(), savedAlert.getStatus());

        // Publicar a RabbitMQ
        publishToRabbitMQ(savedAlert);

        // Enviar email si está configurado y el estado es "down"
        if (emailService != null && "down".equalsIgnoreCase(savedAlert.getStatus())) {
            sendEmailNotification(savedAlert);
        }

        return savedAlert;
    }

    private void publishToRabbitMQ(UptimeAlert alert) {
        try {
            MessageDTO message = new MessageDTO();
            message.setContent(buildAlertMessage(alert));
            message.setSender("Uptime Kuma - " + alert.getMonitorName());
            message.setTimestamp(alert.getAlertTime());

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    RabbitMQConfig.ROUTING_KEY,
                    message
            );
            
            log.info("Alerta publicada a RabbitMQ: {}", alert.getMonitorName());
        } catch (Exception e) {
            log.error("Error publicando alerta a RabbitMQ: {}", e.getMessage(), e);
        }
    }

    private void sendEmailNotification(UptimeAlert alert) {
        try {
            String subject = String.format("🚨 ALERTA: %s está %s", 
                    alert.getMonitorName(), 
                    alert.getStatus().toUpperCase());
            
            emailService.sendMessageNotification(
                    buildAlertMessage(alert),
                    "Uptime Kuma Alert",
                    alert.getAlertTime()
            );
            
            log.info("Email de alerta enviado para: {}", alert.getMonitorName());
        } catch (Exception e) {
            log.error("Error enviando email de alerta: {}", e.getMessage(), e);
        }
    }

    private String buildAlertMessage(UptimeAlert alert) {
        StringBuilder sb = new StringBuilder();
        sb.append("🔔 Alerta de Uptime Kuma\n\n");
        sb.append("Monitor: ").append(alert.getMonitorName()).append("\n");
        sb.append("Estado: ").append(getStatusEmoji(alert.getStatus())).append(" ")
          .append(alert.getStatus().toUpperCase()).append("\n");
        
        if (alert.getMonitorUrl() != null) {
            sb.append("URL: ").append(alert.getMonitorUrl()).append("\n");
        }
        
        if (alert.getPing() != null) {
            sb.append("Tiempo de respuesta: ").append(alert.getPing()).append(" ms\n");
        }
        
        sb.append("Hora: ").append(alert.getAlertTime().format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))).append("\n");
        
        if (alert.getMessage() != null && !alert.getMessage().isEmpty()) {
            sb.append("\nMensaje: ").append(alert.getMessage());
        }
        
        return sb.toString();
    }

    private String getStatusEmoji(String status) {
        if (status == null) return "❓";
        return switch (status.toLowerCase()) {
            case "up" -> "✅";
            case "down" -> "❌";
            case "pending" -> "⏳";
            default -> "❓";
        };
    }

    private String mapStatus(int statusCode) {
        return switch (statusCode) {
            case 1 -> "up";
            case 0 -> "down";
            case 2 -> "pending";
            default -> "unknown";
        };
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        try {
            return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_DATE_TIME);
        } catch (Exception e) {
            log.warn("No se pudo parsear fecha: {}, usando fecha actual", dateTimeStr);
            return LocalDateTime.now();
        }
    }

    public List<UptimeAlert> getAllAlerts() {
        return uptimeAlertRepository.findByOrderByAlertTimeDesc();
    }

    public List<UptimeAlert> getRecentAlerts() {
        return uptimeAlertRepository.findTop10ByOrderByAlertTimeDesc();
    }

    public List<UptimeAlert> getAlertsByMonitor(String monitorName) {
        return uptimeAlertRepository.findByMonitorName(monitorName);
    }

    public List<UptimeAlert> getAlertsByStatus(String status) {
        return uptimeAlertRepository.findByStatus(status);
    }
}
