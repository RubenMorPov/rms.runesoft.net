package net.runesoft.rms.controller;

import lombok.extern.slf4j.Slf4j;
import net.runesoft.rms.config.RabbitMQConfig;
import net.runesoft.rms.entity.Message;
import net.runesoft.rms.model.MessageDTO;
import net.runesoft.rms.service.MessageService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostMapping("/send")
    public ResponseEntity<String> sendMessage(@RequestBody MessageDTO messageDTO) {
        messageDTO.setTimestamp(LocalDateTime.now());
        
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY,
                messageDTO
        );
        
        log.info("Mensaje enviado a RabbitMQ: {}", messageDTO);
        return ResponseEntity.ok("Mensaje enviado correctamente");
    }

    @GetMapping
    public ResponseEntity<List<Message>> getAllMessages() {
        List<Message> messages = messageService.getAllMessages();
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/sender/{sender}")
    public ResponseEntity<List<Message>> getMessagesBySender(@PathVariable String sender) {
        List<Message> messages = messageService.getMessagesBySender(sender);
        return ResponseEntity.ok(messages);
    }
}
