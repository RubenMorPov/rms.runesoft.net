package net.runesoft.rms.repository;

import net.runesoft.rms.entity.UptimeAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UptimeAlertRepository extends JpaRepository<UptimeAlert, Long> {

    List<UptimeAlert> findByMonitorName(String monitorName);
    
    List<UptimeAlert> findByStatus(String status);
    
    List<UptimeAlert> findByAlertTimeBetween(LocalDateTime start, LocalDateTime end);
    
    List<UptimeAlert> findByOrderByAlertTimeDesc();
    
    List<UptimeAlert> findTop10ByOrderByAlertTimeDesc();
}
