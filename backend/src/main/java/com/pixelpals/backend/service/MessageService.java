package com.pixelpals.backend.service;
import com.pixelpals.backend.dto.MessageDTO;
import com.pixelpals.backend.mapper.MessageMapper;
import com.pixelpals.backend.model.Message;
import com.pixelpals.backend.repository.MessageRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final MessageMapper messageMapper;

    public MessageService(MessageRepository messageRepository, MessageMapper messageMapper) {
        this.messageRepository = messageRepository;
        this.messageMapper = messageMapper;
    }

    public MessageDTO sendMessage(MessageDTO dto) {
        dto.setSentAt(LocalDateTime.now());
        Message message = messageMapper.toEntity(dto);
        return messageMapper.toDTO(messageRepository.save(message));
    }

    public List<MessageDTO> getMessagesByMatch(String matchId) {
        return messageRepository.findAll().stream()
                .filter(msg -> msg.getMatch().getId().equals(matchId))
                .map(messageMapper::toDTO)
                .collect(Collectors.toList());
    }
}
