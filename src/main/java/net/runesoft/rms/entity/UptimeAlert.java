package net.runesoft.rms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "uptime_alerts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UptimeAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String monitorName;

    @Column(length = 500)
    private String monitorUrl;

    @Column(nullable = false)
    private String status; // up, down, pending

    private Integer ping; // Response time in ms

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(columnDefinition = "TEXT")
    private String heartbeatData; // JSON completo del heartbeat

    @Column(columnDefinition = "TEXT")
    private String monitorData; // JSON completo del monitor

    @Column(name = "alert_time", nullable = false)
    private LocalDateTime alertTime;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (alertTime == null) {
            alertTime = LocalDateTime.now();
        }
    }
}
