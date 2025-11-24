package net.runesoft.rms.repository;

import net.runesoft.rms.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findBySender(String sender);
    
    List<Message> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    
    List<Message> findByOrderByTimestampDesc();
}
