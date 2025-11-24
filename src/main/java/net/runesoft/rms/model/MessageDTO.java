package net.runesoft.rms.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO implements Serializable {
    
    private Long id;
    private String content;
    private String sender;
    private LocalDateTime timestamp;
}
