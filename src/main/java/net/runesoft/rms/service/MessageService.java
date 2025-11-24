package net.runesoft.rms.service;

import lombok.extern.slf4j.Slf4j;
import net.runesoft.rms.entity.Message;
import net.runesoft.rms.model.MessageDTO;
import net.runesoft.rms.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Transactional
    public Message saveMessage(MessageDTO messageDTO) {
        Message message = new Message();
        message.setContent(messageDTO.getContent());
        message.setSender(messageDTO.getSender());
        message.setTimestamp(messageDTO.getTimestamp());

        Message savedMessage = messageRepository.save(message);
        log.debug("Mensaje guardado en la base de datos: {}", savedMessage);
        
        return savedMessage;
    }

    public List<Message> getAllMessages() {
        return messageRepository.findByOrderByTimestampDesc();
    }

    public List<Message> getMessagesBySender(String sender) {
        return messageRepository.findBySender(sender);
    }
}
