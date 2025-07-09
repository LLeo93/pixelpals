package com.pixelpals.backend.mapper;
import com.pixelpals.backend.dto.MessageDTO;
import com.pixelpals.backend.model.Message;
import com.pixelpals.backend.model.Match;
import com.pixelpals.backend.model.User;
import com.pixelpals.backend.repository.MatchRepository;
import com.pixelpals.backend.repository.UserRepository;
import org.springframework.stereotype.Component;

@Component
public class MessageMapper {

    private final UserRepository userRepository;
    private final MatchRepository matchRepository;

    public MessageMapper(UserRepository userRepository, MatchRepository matchRepository) {
        this.userRepository = userRepository;
        this.matchRepository = matchRepository;
    }

    public Message toEntity(MessageDTO dto) {
        Message message = new Message();
        message.setContent(dto.getContent());
        message.setSentAt(dto.getSentAt());

        userRepository.findById(dto.getSenderId()).ifPresent(message::setSender);
        userRepository.findById(dto.getReceiverId()).ifPresent(message::setReceiver);
        matchRepository.findById(dto.getMatchId()).ifPresent(message::setMatch);

        return message;
    }

    public MessageDTO toDTO(Message message) {
        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setSenderId(message.getSender().getId());
        dto.setReceiverId(message.getReceiver().getId());
        dto.setContent(message.getContent());
        dto.setSentAt(message.getSentAt());
        dto.setMatchId(message.getMatch().getId());
        return dto;
    }
}

