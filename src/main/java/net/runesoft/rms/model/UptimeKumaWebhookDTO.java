package net.runesoft.rms.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UptimeKumaWebhookDTO implements Serializable {
    
    private String heartbeat; // JSON string con datos del heartbeat
    private String monitor; // JSON string con datos del monitor
    private String msg; // Mensaje de texto
    
    // Campos parseados del heartbeat
    private String monitorName;
    private String monitorUrl;
    private String status; // "up", "down", "pending"
    private Integer ping;
    private String time;
    
    // Información adicional
    private String localDateTime;
    private String localTime;
    private String utcDateTime;
    private String utcTime;
}
