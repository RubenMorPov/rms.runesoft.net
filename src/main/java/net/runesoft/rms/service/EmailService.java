package net.runesoft.rms.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@ConditionalOnProperty(name = "email.notification.enabled", havingValue = "true", matchIfMissing = false)
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${email.notification.recipient}")
    private String recipientEmail;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendMessageNotification(String content, String sender, LocalDateTime timestamp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(recipientEmail);
            message.setSubject("Nuevo Mensaje Recibido en RMS - " + sender);
            
            String emailBody = buildEmailBody(content, sender, timestamp);
            message.setText(emailBody);

            mailSender.send(message);
            log.info("Email de notificación enviado exitosamente a: {}", recipientEmail);
        } catch (Exception e) {
            log.error("Error al enviar email de notificación: {}", e.getMessage(), e);
            // No lanzamos excepción para no interrumpir el procesamiento del mensaje
        }
    }

    private String buildEmailBody(String content, String sender, LocalDateTime timestamp) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        
        return String.format("""
                ═══════════════════════════════════════════════════
                NUEVO MENSAJE RECIBIDO EN RMS
                ═══════════════════════════════════════════════════
                
                Remitente: %s
                Fecha: %s
                
                Contenido del mensaje:
                ───────────────────────────────────────────────────
                %s
                ───────────────────────────────────────────────────
                
                Este mensaje ha sido procesado y almacenado en la base de datos.
                
                Saludos,
                Sistema RMS - Runesoft
                """, 
                sender, 
                timestamp.format(formatter), 
                content
        );
    }
}
