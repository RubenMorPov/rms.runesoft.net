package net.runesoft.rms.consumer;

import lombok.extern.slf4j.Slf4j;
import net.runesoft.rms.config.RabbitMQConfig;
import net.runesoft.rms.model.MessageDTO;
import net.runesoft.rms.service.EmailService;
import net.runesoft.rms.service.MessageService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MessageConsumer {

    @Autowired
    private MessageService messageService;

    @Autowired(required = false)
    private EmailService emailService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void receiveMessage(MessageDTO message) {
        log.info("Mensaje recibido de RabbitMQ: {}", message);
        
        try {
            // Procesar el mensaje y guardarlo en la base de datos
            messageService.saveMessage(message);
            log.info("Mensaje procesado y guardado exitosamente: ID={}", message.getId());
            
            // Enviar notificación por email si está habilitado
            if (emailService != null) {
                emailService.sendMessageNotification(
                    message.getContent(), 
                    message.getSender(), 
                    message.getTimestamp()
                );
            }
        } catch (Exception e) {
            log.error("Error procesando mensaje: {}", message, e);
            throw e; // Reenviar excepción para activar reintentos configurados
        }
    }
}
